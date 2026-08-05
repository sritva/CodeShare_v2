import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ReactMarkdown from 'react-markdown'
import CodeMirror from 'codemirror'
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/material-darker.css'
import 'codemirror/mode/clike/clike'
import 'codemirror/mode/python/python'
import 'codemirror/mode/javascript/javascript'
import 'codemirror/mode/xml/xml'
import 'codemirror/mode/css/css'
import 'codemirror/mode/sql/sql'
import api from '../api'

const CM_MODES = {
  java: 'text/x-java',
  python: 'python',
  javascript: 'javascript',
  html: 'xml',
  css: 'css',
  sql: 'text/x-sql',
  c: 'text/x-csrc',
  cpp: 'text/x-c++src',
  text: null
}

export default function ViewSnippet() {
  const { id } = useParams()
  const { username, isLoggedIn } = useAuth()
  const navigate = useNavigate()
  const [snippet, setSnippet] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [explanation, setExplanation] = useState('')
  const [explaining, setExplaining] = useState(false)
  const [explainError, setExplainError] = useState('')
  const [copied, setCopied] = useState(false)
  const [shareToken, setShareToken] = useState(null)
  const [shareEnabled, setShareEnabled] = useState(false)
  const [shareLoading, setShareLoading] = useState(false)
  const [shareCopied, setShareCopied] = useState(false)
  const [shareError, setShareError] = useState('')
  const [showAiExplanation, setShowAiExplanation] = useState(true)

  const editorRef = useRef(null)
  const cmRef = useRef(null)

  useEffect(() => {
    api.get(`/snippets/${id}`)
      .then(res => {
        setSnippet(res.data)
        if (res.data.aiExplanation) {
          setExplanation(res.data.aiExplanation)
        }
        if (res.data.shareToken && res.data.shareEnabled) {
          setShareToken(res.data.shareToken)
          setShareEnabled(res.data.shareEnabled)
        }
      })
      .catch(() => setError('Snippet not found or access denied'))
      .finally(() => setLoading(false))
  }, [id])

  useEffect(() => {
    if (snippet && editorRef.current && !cmRef.current) {
      cmRef.current = CodeMirror.fromTextArea(editorRef.current, {
        value: snippet.code,
        theme: 'material-darker',
        lineNumbers: true,
        lineWrapping: true,
        readOnly: 'nocursor',
        mode: CM_MODES[snippet.language] || null
      })
      cmRef.current.setSize(null, 'auto')
    }
    return () => {
      if (cmRef.current) {
        cmRef.current.toTextArea()
        cmRef.current = null
      }
    }
  }, [snippet])

  const handleExplain = async () => {
    setExplaining(true)
    setExplainError('')
    setShowAiExplanation(true)
    try {
      const res = await api.post(`/snippets/${id}/explain`)
      setExplanation(res.data.explanation)
    } catch (err) {
      setExplainError(
        err.response?.data?.error || 'Failed to generate explanation'
      )
    } finally {
      setExplaining(false)
    }
  }

  const handleCopy = () => {
    navigator.clipboard.writeText(snippet.code)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const handleShare = async () => {
    setShareLoading(true)
    setShareError('')
    try {
      const res = await api.post(`/snippets/${id}/share`)
      setShareToken(res.data.shareToken)
      setShareEnabled(true)
    } catch (err) {
      setShareError(
        err.response?.data?.error || 'Failed to generate share link'
      )
    } finally {
      setShareLoading(false)
    }
  }

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this snippet?')) {
      return
    }
    try {
      await api.delete(`/snippets/${id}`)
      navigate('/my-snippets')
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to delete snippet')
    }
  }

  const handleUnshare = async () => {
    setShareLoading(true)
    try {
      await api.post(`/snippets/${id}/unshare`)
      setShareEnabled(false)
    } catch (err) {
      setShareError('Failed to disable sharing')
    } finally {
      setShareLoading(false)
    }
  }

  const handleCopyShareLink = () => {
    const url = `${window.location.origin}/share/snippet/${shareToken}`
    navigator.clipboard.writeText(url)
    setShareCopied(true)
    setTimeout(() => setShareCopied(false), 2000)
  }

  const isOwner = isLoggedIn && snippet?.username === username

  if (loading) {
    return (
      <div className="text-center text-gray-500 py-16">
        Loading snippet...
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center">
        <p className="text-red-400 mb-4">{error}</p>
        <Link to="/home"
          className="text-indigo-400 hover:text-indigo-300 text-sm">
          Back to home
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">

      {/* Header */}
      <div className="flex items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white mb-1">
            {snippet.title}
          </h1>
          <div className="flex items-center gap-3 text-sm text-gray-400">
            <span>by {snippet.username}</span>
            <span>{"\u00B7"}</span>
            <span className="font-mono bg-gray-800 px-2 py-0.5 rounded">
              {snippet.language}
            </span>
            <span>{"\u00B7"}</span>
            <span>
              {new Date(snippet.createdAt).toLocaleDateString()}
            </span>
            <span className={`px-2 py-0.5 rounded text-xs
              ${snippet.public
                ? 'bg-green-900/40 text-green-400'
                : 'bg-gray-800 text-gray-500'}`}>
              {snippet.public ? 'Public' : 'Private'}
            </span>
          </div>
        </div>
        {isOwner && (
          <div className="flex gap-2 shrink-0">
            <Link
              to={`/edit/${snippet.id}`}
              className="text-sm bg-gray-800 hover:bg-gray-700 
                text-gray-300 px-3 py-1.5 rounded-lg transition-colors">
              Edit
            </Link>
            <button
              onClick={handleDelete}
              className="text-sm bg-red-950/40 hover:bg-red-900/60 
                text-red-400 border border-red-800/60 px-3 py-1.5 rounded-lg transition-colors">
              Delete
            </button>
          </div>
        )}
      </div>

      {/* Code block */}
      <div className="relative bg-gray-900 border border-gray-700 
        rounded-xl overflow-hidden mb-6 cm-read-only">
        <div className="flex items-center justify-between px-4 py-2 
          border-b border-gray-700 bg-gray-800">
          <span className="text-xs text-gray-400 font-mono">
            {snippet.language}
          </span>
          <div className="relative">
            <button
              onClick={handleCopy}
              className="text-xs text-gray-400 hover:text-white 
                transition-colors">
              {copied ? '✓ Copied' : 'Copy'}
            </button>
            {copied && (
              <span className="absolute bottom-full right-0 mb-2 px-2 py-1 
                bg-indigo-600 text-white text-xs rounded shadow-lg border border-indigo-500 
                whitespace-nowrap animate-bounce">
                Copied to clipboard!
              </span>
            )}
          </div>
        </div>
        <div className="p-4 bg-gray-900">
          <textarea
            ref={editorRef}
            defaultValue={snippet.code}
          />
        </div>
      </div>

      {/* Share section */}
      {isLoggedIn && (snippet?.public || isOwner) && (
        <div className="bg-gray-900 border border-gray-700 
          rounded-xl p-5 mb-6">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-sm font-medium text-gray-300">
              Share Snippet
            </h2>
            {!shareEnabled ? (
              <button
                onClick={handleShare}
                disabled={shareLoading}
                className="text-xs bg-gray-700 hover:bg-gray-600 
                  disabled:opacity-50 text-white px-3 py-1.5 
                  rounded-lg transition-colors">
                {shareLoading ? 'Generating...' : 'Generate share link'}
              </button>
            ) : (
              <button
                onClick={handleUnshare}
                disabled={shareLoading}
                className="text-xs bg-red-900/30 hover:bg-red-900/50 
                  disabled:opacity-50 text-red-400 px-3 py-1.5 
                  rounded-lg transition-colors">
                {shareLoading ? 'Disabling...' : 'Disable sharing'}
              </button>
            )}
          </div>

          {shareError && (
            <p className="text-red-400 text-xs mb-2">{shareError}</p>
          )}

          {shareEnabled && shareToken && (
            <div className="flex items-center gap-2">
              <code className="flex-1 bg-gray-800 text-gray-300 text-xs 
                px-3 py-2 rounded-lg truncate font-mono">
                {`${window.location.origin}/share/snippet/${shareToken}`}
              </code>
              <div className="relative shrink-0">
                <button
                  onClick={handleCopyShareLink}
                  className="text-xs bg-indigo-600 
                    hover:bg-indigo-500 text-white px-3 py-2 
                    rounded-lg transition-colors">
                  {shareCopied ? '✓ Copied' : 'Copy'}
                </button>
                {shareCopied && (
                  <span className="absolute bottom-full right-0 mb-2 px-2 py-1 
                    bg-indigo-600 text-white text-xs rounded shadow-lg border border-indigo-500 
                    whitespace-nowrap animate-bounce">
                    Link Copied!
                  </span>
                )}
              </div>
            </div>
          )}

          {!shareEnabled && !shareToken && (
            <p className="text-gray-600 text-xs">
              Generate a link to share this snippet with anyone,
              even without an account.
            </p>
          )}
        </div>
      )}

      {/* AI Explanation Accordion */}
      <div className="bg-gray-900 border border-gray-700 rounded-xl p-5 mb-6">
        <div className="flex items-center justify-between">
          <div 
            onClick={() => setShowAiExplanation(prev => !prev)}
            className="flex items-center gap-2 cursor-pointer select-none group"
          >
            <span className={`text-xs text-gray-400 group-hover:text-white transition-transform duration-200 ${showAiExplanation ? 'rotate-90' : 'rotate-0'}`}>
              ▶
            </span>
            <h2 className="text-sm font-medium text-gray-300 group-hover:text-white transition-colors">
              AI Explanation
            </h2>
          </div>
          <div className="flex items-center gap-3">
            {isLoggedIn && (
              <button
                onClick={handleExplain}
                disabled={explaining}
                className="text-xs bg-indigo-600 hover:bg-indigo-500 
                  disabled:opacity-50 text-white px-3 py-1.5 
                  rounded-lg transition-colors">
                {explaining
                  ? 'Generating...'
                  : explanation
                    ? 'Regenerate'
                    : 'Explain this code'}
              </button>
            )}
            {!isLoggedIn && (
              <Link to="/login"
                className="text-xs text-indigo-400 hover:text-indigo-300">
                Sign in to use AI explanation
              </Link>
            )}
          </div>
        </div>

        {/* Accordion Content with Height/Opacity transition */}
        <div className={`transition-all duration-300 overflow-hidden ${showAiExplanation ? 'mt-4 max-h-[1500px] opacity-100' : 'max-h-0 opacity-0'}`}>
          {explainError && (
            <p className="text-red-400 text-sm">{explainError}</p>
          )}

          {explaining && (
            <p className="text-gray-500 text-sm">
              Analyzing your code...
            </p>
          )}

          {explanation && !explaining && (
            <div className="text-gray-300 text-sm leading-relaxed 
              prose prose-invert prose-sm max-w-none border-t border-gray-800 pt-4">
              <ReactMarkdown
                components={{
                  code: ({node, className, children, ...props}) => {
                    const match = /language-(\w+)/.exec(className || '')
                    return !match ? (
                      <code className="bg-gray-800 text-indigo-300 px-1.5 py-0.5 rounded text-xs font-mono" {...props}>
                        {children}
                      </code>
                    ) : (
                      <pre className="bg-gray-800 rounded-lg p-3 overflow-x-auto my-2">
                        <code className={`${className} text-xs font-mono text-gray-300`} {...props}>
                          {children}
                        </code>
                      </pre>
                    )
                  },
                  strong: ({children}) => (
                    <strong className="text-white font-semibold">
                      {children}
                    </strong>
                  ),
                  p: ({children}) => (
                    <div className="mb-2 last:mb-0">{children}</div>
                  ),
                  ul: ({children}) => (
                    <ul className="list-disc list-inside space-y-1 mb-2">
                      {children}
                    </ul>
                  ),
                  ol: ({children}) => (
                    <ol className="list-decimal list-inside space-y-1 mb-2">
                      {children}
                    </ol>
                  ),
                  li: ({children}) => (
                    <li className="text-gray-300">{children}</li>
                  ),
                }}
              >
                {explanation}
              </ReactMarkdown>
            </div>
          )}

          {!explanation && !explaining && !explainError && (
            <p className="text-gray-600 text-sm border-t border-gray-800 pt-4">
              {isLoggedIn
                ? 'Click "Explain this code" to get an AI-powered explanation.'
                : 'Sign in to get an AI-powered explanation of this snippet.'}
            </p>
          )}
        </div>
      </div>

      <div className="mt-6">
        <Link to="/home"
          className="text-sm text-gray-500 hover:text-gray-300 
            transition-colors">
          {"\u2190"} Back to snippets
        </Link>
      </div>
    </div>
  )
}
