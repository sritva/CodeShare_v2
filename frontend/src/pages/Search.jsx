import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../api'

const LANGUAGES = [
  'all','java','python','javascript',
  'html','css','sql','c','cpp','text'
]

function SnippetCard({ snippet }) {
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

      <div className="mt-3 flex items-center justify-between border-t border-gray-800/50 pt-3">
        <span className="text-xs text-gray-500">
          by{' '}
          <Link to={`/user/${snippet.username}`} className="text-indigo-400 hover:text-indigo-300 font-medium">
            @{snippet.username}
          </Link>
        </span>
        <span className="text-xs text-gray-600">
          {new Date(snippet.createdAt).toLocaleDateString()}
        </span>
      </div>
    </div>
  )
}

export default function Search() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [keyword, setKeyword] = useState(searchParams.get('q') || '')
  const [language, setLanguage] = useState(
    searchParams.get('lang') || 'all'
  )
  const [snippets, setSnippets] = useState([])
  const [totalResults, setTotalResults] = useState(0)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(false)
  const [searched, setSearched] = useState(false)

  const doSearch = (kw, lang, pg) => {
    setLoading(true)
    setSearched(true)
    api.get('/snippets/search', {
      params: { keyword: kw, language: lang, page: pg }
    })
      .then(res => {
        setSnippets(res.data.snippets)
        setTotalResults(res.data.totalResults)
        setTotalPages(res.data.totalPages)
      })
      .catch(() => setSnippets([]))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    const q = searchParams.get('q')
    const lang = searchParams.get('lang')
    if (q || lang) {
      doSearch(q || '', lang || 'all', 0)
    }
  }, [])

  const handleSubmit = (e) => {
    e.preventDefault()
    setPage(0)
    setSearchParams({ q: keyword, lang: language })
    doSearch(keyword, language, 0)
  }

  const handlePage = (newPage) => {
    setPage(newPage)
    doSearch(keyword, language, newPage)
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-white mb-6">Search</h1>

      <form onSubmit={handleSubmit}
        className="flex gap-3 mb-8 flex-wrap">
        <input
          type="text"
          value={keyword}
          onChange={e => setKeyword(e.target.value)}
          placeholder="Search snippets..."
          className="flex-1 min-w-48 bg-gray-800 border border-gray-600 
            rounded-lg px-4 py-2.5 text-white text-sm 
            focus:outline-none focus:border-indigo-500"
        />
        <select
          value={language}
          onChange={e => setLanguage(e.target.value)}
          className="bg-gray-800 border border-gray-600 rounded-lg 
            px-4 py-2.5 text-white text-sm focus:outline-none 
            focus:border-indigo-500">
          {LANGUAGES.map(l => (
            <option key={l} value={l}>
              {l === 'all' ? 'All languages' : l}
            </option>
          ))}
        </select>
        <button
          type="submit"
          className="bg-indigo-600 hover:bg-indigo-500 text-white 
            font-medium px-6 py-2.5 rounded-lg transition-colors">
          Search
        </button>
      </form>

      {loading && (
        <div className="text-center text-gray-500 py-16">
          Searching...
        </div>
      )}

      {searched && !loading && (
        <p className="text-sm text-gray-500 mb-4">
          {totalResults} result{totalResults !== 1 ? 's' : ''} found
        </p>
      )}

      <div className="grid gap-4">
        {snippets.map(snippet => (
          <SnippetCard key={snippet.id} snippet={snippet} />
        ))}
      </div>

      {searched && !loading && snippets.length === 0 && (
        <div className="text-center text-gray-500 py-16">
          No snippets found. Try a different search.
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-3 mt-8">
          <button
            onClick={() => handlePage(page - 1)}
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
            onClick={() => handlePage(page + 1)}
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
