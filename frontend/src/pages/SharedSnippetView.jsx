import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import api from '../api'

export default function SharedSnippetView() {
  const { token } = useParams()
  const [snippet, setSnippet] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [copied, setCopied] = useState(false)

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

  const handleCopy = () => {
    navigator.clipboard.writeText(snippet.code)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

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
          </div>
        </div>
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
            className="text-xs text-gray-400 hover:text-white transition-colors">
            {copied ? '✓ Copied' : 'Copy'}
          </button>
        </div>
        <pre className="p-4 overflow-x-auto text-sm text-gray-300
          font-mono leading-relaxed whitespace-pre">
          {snippet.code}
        </pre>
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
          ← Browse snippets
        </Link>
        <Link to="/register"
          className="text-sm text-indigo-400 hover:text-indigo-300">
          Create your own snippets →
        </Link>
      </div>
    </div>
  )
}
