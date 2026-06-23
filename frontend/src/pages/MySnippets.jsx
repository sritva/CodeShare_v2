import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api'

function SnippetRow({ snippet, onDelete }) {
  const [deleting, setDeleting] = useState(false)

  const handleDelete = async () => {
    if (!window.confirm('Delete this snippet?')) return
    setDeleting(true)
    try {
      await api.delete(`/snippets/${snippet.id}`)
      onDelete(snippet.id)
    } catch {
      alert('Failed to delete snippet')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="bg-gray-900 border border-gray-700 rounded-xl 
      p-5 flex items-start justify-between gap-4">
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <Link
            to={`/snippet/${snippet.id}`}
            className="text-white font-medium hover:text-indigo-400 
              transition-colors truncate">
            {snippet.title}
          </Link>
          <span className="shrink-0 text-xs bg-gray-800 text-gray-400 
            px-2 py-0.5 rounded font-mono">
            {snippet.language}
          </span>
          <span className={`shrink-0 text-xs px-2 py-0.5 rounded 
            ${snippet.public
              ? 'bg-green-900/40 text-green-400'
              : 'bg-gray-800 text-gray-500'}`}>
            {snippet.public ? 'Public' : 'Private'}
          </span>
        </div>
        <p className="text-xs text-gray-500">
          {new Date(snippet.createdAt).toLocaleDateString()}
        </p>
      </div>
      <div className="flex items-center gap-2 shrink-0">
        <Link
          to={`/edit/${snippet.id}`}
          className="text-sm text-gray-400 hover:text-white 
            bg-gray-800 hover:bg-gray-700 px-3 py-1.5 
            rounded-lg transition-colors">
          Edit
        </Link>
        <button
          onClick={handleDelete}
          disabled={deleting}
          className="text-sm text-red-400 hover:text-white 
            bg-red-900/20 hover:bg-red-900/40 px-3 py-1.5 
            rounded-lg transition-colors disabled:opacity-50">
          {deleting ? 'Deleting...' : 'Delete'}
        </button>
      </div>
    </div>
  )
}

export default function MySnippets() {
  const [snippets, setSnippets] = useState([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    api.get('/snippets/mine')
      .then(res => setSnippets(res.data))
      .catch(() => setSnippets([]))
      .finally(() => setLoading(false))
  }, [])

  const handleDelete = (id) => {
    setSnippets(s => s.filter(sn => sn.id !== id))
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-white mb-1">
            My Snippets
          </h1>
          <p className="text-gray-400">
            {snippets.length} snippet{snippets.length !== 1 ? 's' : ''}
          </p>
        </div>
        <button
          onClick={() => navigate('/create')}
          className="bg-indigo-600 hover:bg-indigo-500 text-white 
            font-medium px-4 py-2.5 rounded-lg transition-colors">
          + New Snippet
        </button>
      </div>

      {loading && (
        <div className="text-center text-gray-500 py-16">
          Loading your snippets...
        </div>
      )}

      {!loading && snippets.length === 0 && (
        <div className="text-center py-16">
          <p className="text-gray-500 mb-4">
            You haven't created any snippets yet.
          </p>
          <button
            onClick={() => navigate('/create')}
            className="bg-indigo-600 hover:bg-indigo-500 text-white 
              font-medium px-4 py-2.5 rounded-lg transition-colors">
            Create your first snippet
          </button>
        </div>
      )}

      <div className="space-y-3">
        {snippets.map(snippet => (
          <SnippetRow
            key={snippet.id}
            snippet={snippet}
            onDelete={handleDelete}
          />
        ))}
      </div>
    </div>
  )
}
