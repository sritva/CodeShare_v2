import { useState, useEffect, useRef } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
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

export default function SharedSnippetView() {
  const { token } = useParams()
  const { isLoggedIn, username } = useAuth()
  const navigate = useNavigate()
  const [snippet, setSnippet] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [copied, setCopied] = useState(false)
  const [forkLoading, setForkLoading] = useState(false)

  const editorRef = useRef(null)
  const cmRef = useRef(null)

  useEffect(() => {
    api.get(`/snippets/shared/${token}`)
      .then(res => setSnippet(res.data))
      .catch(err => {
        if (err.response?.status === 410) {
          setError('This share link has been disabled by the owner.')
        } else {
          setError('Snippet not found or this link is no longer valid.')
        }
      })
      .finally(() => setLoading(false))
  }, [token])

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

  const handleCopy = () => {
    navigator.clipboard.writeText(snippet.code)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const handleFork = async () => {
    if (!isLoggedIn) {
      navigate('/login')
      return
    }
    if (!window.confirm('Would you like to fork this snippet to your profile?')) return
    setForkLoading(true)
    try {
      const res = await api.post(`/snippets/${snippet.id}/fork`)
      navigate(`/snippet/${res.data.id}`)
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to fork snippet')
    } finally {
      setForkLoading(false)
    }
  }

  const isOwner = isLoggedIn && snippet?.username === username

  if (loading) return (
    <div className="text-center text-gray-500 py-16">
      Loading...
    </div>
  )

  if (error) return (
    <div className="max-w-3xl mx-auto px-4 py-16 text-center">
      <p className="text-red-400 mb-4">{error}</p>
      <Link to="/home"
        className="text-indigo-400 hover:text-indigo-300 text-sm">
        Browse public snippets
      </Link>
    </div>
  )

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">

      {/* Header */}
      <div className="flex items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white mb-1">
            {snippet.title}
          </h1>
          {snippet.parentId && (
            <div className="text-xs text-gray-500 mb-2">
              🍴 Forked from{' '}
              <Link to={`/snippet/${snippet.parentId}`} className="text-indigo-400 hover:underline">
                @{snippet.parentUsername}/{snippet.parentTitle}
              </Link>
            </div>
          )}
          <div className="flex items-center gap-3 text-sm text-gray-400">
            <span>by <Link to={`/user/${snippet.username}`} className="text-indigo-400 hover:text-indigo-300 font-medium">@{snippet.username}</Link></span>
            <span>{"\u00B7"}</span>
            <span className="font-mono bg-gray-800 px-2 py-0.5 rounded">
              {snippet.language}
            </span>
            <span>{"\u00B7"}</span>
            <span>
              {new Date(snippet.createdAt).toLocaleDateString()}
            </span>
          </div>
        </div>
        <div className="flex gap-2 shrink-0">
          {!isOwner && (
            <button
              onClick={handleFork}
              disabled={forkLoading}
              className="text-sm bg-indigo-650 hover:bg-indigo-600 disabled:opacity-50
                text-white px-3 py-1.5 rounded-lg transition-all font-medium shadow-md hover:shadow-indigo-900/40"
            >
              {forkLoading ? 'Forking...' : 'Fork'}
            </button>
          )}
        </div>
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
              className="text-xs text-gray-400 hover:text-white transition-colors">
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

      {/* AI Explanation — shown only if one already exists */}
      {snippet.aiExplanation && (
        <div className="bg-gray-900 border border-gray-700 rounded-xl p-5 mb-6">
          <h2 className="text-sm font-medium text-gray-300 mb-3">
            AI Explanation
          </h2>
          <div className="text-gray-300 text-sm leading-relaxed
            prose prose-invert prose-sm max-w-none">
            <ReactMarkdown
              components={{
                code: ({ node, className, children, ...props }) => {
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
                strong: ({ children }) => (
                  <strong className="text-white font-semibold">{children}</strong>
                ),
                p: ({ children }) => (
                  <div className="mb-2 last:mb-0">{children}</div>
                ),
                ul: ({ children }) => (
                  <ul className="list-disc list-inside space-y-1 mb-2">{children}</ul>
                ),
                ol: ({ children }) => (
                  <ol className="list-decimal list-inside space-y-1 mb-2">{children}</ol>
                ),
                li: ({ children }) => (
                  <li className="text-gray-300">{children}</li>
                ),
              }}
            >
              {snippet.aiExplanation}
            </ReactMarkdown>
          </div>
        </div>
      )}

      <div className="mt-6 flex items-center justify-between">
        <Link to="/home"
          className="text-sm text-gray-500 hover:text-gray-300 transition-colors">
          {"\u2190"} Browse snippets
        </Link>
        <Link to="/register"
          className="text-sm text-indigo-400 hover:text-indigo-300">
          Create your own snippets →
        </Link>
      </div>
    </div>
  )
}
