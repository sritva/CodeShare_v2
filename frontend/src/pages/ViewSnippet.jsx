import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ReactMarkdown from 'react-markdown'
import api from '../api'

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

  useEffect(() => {
    api.get(`/snippets/${id}`)
      .then(res => {
        setSnippet(res.data)
        if (res.data.aiExplanation) {
          setExplanation(res.data.aiExplanation)
        }
      })
      .catch(() => setError('Snippet not found or access denied'))
      .finally(() => setLoading(false))
  }, [id])

  const handleExplain = async () => {
    setExplaining(true)
    setExplainError('')
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
            <span>·</span>
            <span className="font-mono bg-gray-800 px-2 py-0.5 rounded">
              {snippet.language}
            </span>
            <span>·</span>
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
          </div>
        )}
      </div>

      {/* Code block */}
      <div className="relative bg-gray-900 border border-gray-700 
        rounded-xl overflow-hidden mb-6">
        <div className="flex items-center justify-between px-4 py-2 
          border-b border-gray-700 bg-gray-800">
          <span className="text-xs text-gray-400 font-mono">
            {snippet.language}
          </span>
          <button
            onClick={handleCopy}
            className="text-xs text-gray-400 hover:text-white 
              transition-colors">
            {copied ? '✓ Copied' : 'Copy'}
          </button>
        </div>
        <pre className="p-4 overflow-x-auto text-sm text-gray-300 
          font-mono leading-relaxed whitespace-pre">
          {snippet.code}
        </pre>
      </div>

      {/* AI Explanation */}
      <div className="bg-gray-900 border border-gray-700 rounded-xl p-5">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-sm font-medium text-gray-300">
            AI Explanation
          </h2>
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
            prose prose-invert prose-sm max-w-none">
            <ReactMarkdown
              components={{
                code: ({node, inline, className, children, ...props}) => (
                  inline
                    ? <code className="bg-gray-800 text-indigo-300 px-1.5 
                        py-0.5 rounded text-xs font-mono" {...props}>
                        {children}
                      </code>
                    : <pre className="bg-gray-800 rounded-lg p-3 
                        overflow-x-auto my-2">
                        <code className="text-xs font-mono text-gray-300" 
                          {...props}>
                          {children}
                        </code>
                      </pre>
                ),
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
          <p className="text-gray-600 text-sm">
            {isLoggedIn
              ? 'Click "Explain this code" to get an AI-powered explanation.'
              : 'Sign in to get an AI-powered explanation of this snippet.'}
          </p>
        )}
      </div>

      <div className="mt-6">
        <Link to="/home"
          className="text-sm text-gray-500 hover:text-gray-300 
            transition-colors">
          ← Back to snippets
        </Link>
      </div>
    </div>
  )
}
