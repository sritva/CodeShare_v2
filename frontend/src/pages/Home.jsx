import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'

function SnippetCard({ snippet }) {
  return (
    <Link to={`/snippet/${snippet.id}`}
      className="block bg-gray-900 border border-gray-700 rounded-xl 
        p-5 hover:border-indigo-500 transition-colors group">
      <div className="flex items-start justify-between gap-4 mb-3">
        <h2 className="text-white font-medium group-hover:text-indigo-400 
          transition-colors line-clamp-1">
          {snippet.title}
        </h2>
        <span className="shrink-0 text-xs bg-gray-800 text-gray-400 
          px-2 py-1 rounded-md font-mono">
          {snippet.language}
        </span>
      </div>
      <pre className="text-gray-400 text-xs font-mono bg-gray-800 
        rounded-lg p-3 overflow-hidden line-clamp-4 whitespace-pre-wrap">
        {snippet.code}
      </pre>
      <div className="mt-3 flex items-center justify-between">
        <span className="text-xs text-gray-500">
          by {snippet.username}
        </span>
        <span className="text-xs text-gray-600">
          {new Date(snippet.createdAt).toLocaleDateString()}
        </span>
      </div>
    </Link>
  )
}

export default function Home() {
  const [snippets, setSnippets] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    api.get(`/snippets/public?page=${page}`)
      .then(res => {
        setSnippets(res.data.snippets)
        setTotalPages(res.data.totalPages)
      })
      .catch(() => setError('Failed to load snippets'))
      .finally(() => setLoading(false))
  }, [page])

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white mb-2">
          Community Snippets
        </h1>
        <p className="text-gray-400">
          Browse code shared by the community
        </p>
      </div>

      {loading && (
        <div className="text-center text-gray-500 py-16">
          Loading snippets...
        </div>
      )}

      {error && (
        <div className="bg-red-900/40 border border-red-700 text-red-300 
          rounded-lg px-4 py-3 text-sm">
          {error}
        </div>
      )}

      {!loading && !error && snippets.length === 0 && (
        <div className="text-center text-gray-500 py-16">
          No snippets yet. Be the first to share one.
        </div>
      )}

      <div className="grid gap-4">
        {snippets.map(snippet => (
          <SnippetCard key={snippet.id} snippet={snippet} />
        ))}
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-3 mt-8">
          <button
            onClick={() => setPage(p => p - 1)}
            disabled={page === 0}
            className="px-4 py-2 bg-gray-800 text-gray-300 rounded-lg 
              text-sm disabled:opacity-40 hover:bg-gray-700 
              transition-colors">
            Previous
          </button>
          <span className="text-sm text-gray-500">
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => setPage(p => p + 1)}
            disabled={page + 1 >= totalPages}
            className="px-4 py-2 bg-gray-800 text-gray-300 rounded-lg 
              text-sm disabled:opacity-40 hover:bg-gray-700 
              transition-colors">
            Next
          </button>
        </div>
      )}
    </div>
  )
}
