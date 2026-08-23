import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'

function StarredCard({ snippet, onUnstar }) {
  const [unstarring, setUnstarring] = useState(false)

  const handleUnstarClick = async (e) => {
    e.preventDefault()
    e.stopPropagation()
    if (!window.confirm('Remove this snippet from your starred list?')) return
    setUnstarring(true)
    try {
      await api.post(`/snippets/${snippet.id}/unstar`)
      onUnstar(snippet.id)
    } catch {
      alert('Failed to unstar snippet')
    } finally {
      setUnstarring(false)
    }
  }

  return (
    <div className="bg-gray-900 border border-gray-700 rounded-xl p-5 hover:border-indigo-500/50 transition-colors group flex flex-col justify-between">
      <div>
        <div className="flex items-start justify-between gap-4 mb-3">
          <Link to={`/snippet/${snippet.id}`} className="block flex-1">
            <h2 className="text-white font-medium group-hover:text-indigo-400 transition-colors line-clamp-1">
              {snippet.title}
            </h2>
          </Link>
          <span className="shrink-0 text-xs bg-gray-800 text-gray-400 px-2 py-1 rounded-md font-mono">
            {snippet.language}
          </span>
        </div>

        {snippet.parentIdValue && (
          <div className="text-[10px] text-gray-500 mb-2">
            🍴 Forked from{' '}
            <Link to={`/snippet/${snippet.parentIdValue}`} className="text-indigo-400 hover:underline">
              @{snippet.parentUsername}/{snippet.parentTitle}
            </Link>
          </div>
        )}

        <Link to={`/snippet/${snippet.id}`} className="block">
          <pre className="code-preview-box text-xs font-mono border rounded-lg p-3 overflow-hidden line-clamp-3 whitespace-pre-wrap">
            {snippet.code}
          </pre>
        </Link>
      </div>

      <div className="mt-4 flex items-center justify-between border-t border-gray-800/50 pt-3">
        <span className="text-xs text-gray-500">
          by{' '}
          <Link to={`/user/${snippet.username}`} className="text-indigo-400 hover:text-indigo-300 font-medium">
            @{snippet.username}
          </Link>
        </span>
        <div className="flex items-center gap-3">
          <button
            onClick={handleUnstarClick}
            disabled={unstarring}
            className="text-xs text-amber-400 hover:text-amber-300 bg-gray-800/50 hover:bg-gray-800 px-2.5 py-1 rounded-md border border-gray-750 flex items-center gap-1.5 transition-colors"
            title="Unstar snippet"
          >
            <svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 24 24" className="w-3.5 h-3.5 text-amber-400">
              <path fillRule="evenodd" d="M10.788 3.21c.448-1.077 1.976-1.077 2.424 0l2.082 5.007 5.404.433c1.164.093 1.636 1.545.749 2.305l-4.117 3.527 1.257 5.273c.271 1.136-.964 2.033-1.96 1.425L12 18.354 7.373 21.18c-.996.608-2.231-.29-1.96-1.425l1.257-5.273-4.117-3.527c-.887-.76-.415-2.212.749-2.305l5.404-.433 2.082-5.006z" clipRule="evenodd" />
            </svg>
            <span>{unstarring ? '...' : 'Starred'}</span>
          </button>
          <span className="text-[11px] text-gray-600">
            {new Date(snippet.createdAt).toLocaleDateString()}
          </span>
        </div>
      </div>
    </div>
  )
}

export default function StarredSnippets() {
  const [snippets, setSnippets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/snippets/starred')
      .then(res => {
        setSnippets(res.data)
      })
      .catch(() => {
        setError('Failed to load starred snippets')
      })
      .finally(() => {
        setLoading(false)
      })
  }, [])

  const handleUnstar = (id) => {
    setSnippets(prev => prev.filter(s => s.id !== id))
  }

  if (loading) {
    return (
      <div className="text-center text-gray-500 py-16">
        Loading starred snippets...
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <p className="text-red-400 mb-4">{error}</p>
        <Link to="/home" className="text-indigo-400 hover:text-indigo-300 text-sm">
          Return to browse public snippets
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-white mb-2">Starred Snippets</h1>
        <p className="text-gray-400 text-sm">
          Your bookmarked community code snippets ({snippets.length})
        </p>
      </div>

      {snippets.length === 0 ? (
        <div className="text-center py-16 bg-gray-900 border border-gray-700 rounded-xl">
          <p className="text-gray-500 text-base mb-4">No starred snippets yet.</p>
          <Link
            to="/home"
            className="inline-flex items-center text-sm bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg font-semibold transition-colors"
          >
            Explore Public Snippets
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {snippets.map((snippet) => (
            <StarredCard
              key={snippet.id}
              snippet={snippet}
              onUnstar={handleUnstar}
            />
          ))}
        </div>
      )}
    </div>
  )
}
