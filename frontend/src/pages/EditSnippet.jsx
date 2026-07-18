import { useState, useEffect, useRef } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
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

const LANGUAGES = [
  'text','java','python','javascript',
  'html','css','sql','c','cpp'
]

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

export default function EditSnippet() {
  const { id } = useParams()
  const [form, setForm] = useState({
    title: '', language: 'text', isPublic: true
  })
  const [initialCode, setInitialCode] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [editorRef, cmRef] = [useRef(null), useRef(null)]
  const navigate = useNavigate()

  useEffect(() => {
    api.get(`/snippets/${id}`)
      .then(res => {
        const s = res.data
        setForm({
          title: s.title,
          language: s.language,
          isPublic: s.public
        })
        setInitialCode(s.code)
        setLoading(false)
        
        setTimeout(() => {
          if (editorRef.current && !cmRef.current) {
            cmRef.current = CodeMirror.fromTextArea(editorRef.current, {
              theme: 'material-darker',
              lineNumbers: true,
              indentUnit: 4,
              lineWrapping: true,
            })
            cmRef.current.setValue(s.code)
            cmRef.current.setSize(null, 400)
            cmRef.current.setOption('mode', CM_MODES[s.language] || null)
          }
        }, 50)
      })
      .catch(() => {
        setError('Failed to load snippet')
        setLoading(false)
      })
  }, [id])

  const handleChange = e => {
    const { name, value, type, checked } = e.target
    setForm(f => ({ ...f, [name]: type === 'checkbox' ? checked : value }))
    if (name === 'language' && cmRef.current) {
      cmRef.current.setOption('mode', CM_MODES[value] || null)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const code = cmRef.current 
      ? cmRef.current.getValue() 
      : (editorRef.current ? editorRef.current.value : '')
    if (!code.trim()) {
      setError('Code cannot be blank')
      return
    }
    setError('')
    setSaving(true)
    try {
      await api.put(`/snippets/${id}`, { ...form, code })
      navigate('/my-snippets')
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save changes')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="text-center text-gray-500 py-16">
        Loading snippet...
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-white mb-6">
        Edit Snippet
      </h1>
      {error && (
        <div className="bg-red-900/40 border border-red-700 
          text-red-300 rounded-lg px-4 py-3 mb-4 text-sm">
          {error}
        </div>
      )}
      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="block text-sm text-gray-400 mb-1">
            Title
          </label>
          <input
            name="title"
            value={form.title}
            onChange={handleChange}
            maxLength={200}
            required
            className="w-full bg-gray-800 border border-gray-600 
              rounded-lg px-4 py-2.5 text-white text-sm 
              focus:outline-none focus:border-indigo-500"
          />
        </div>
        <div>
          <label className="block text-sm text-gray-400 mb-1">
            Language
          </label>
          <select
            name="language"
            value={form.language}
            onChange={handleChange}
            className="bg-gray-800 border border-gray-600 rounded-lg 
              px-4 py-2.5 text-white text-sm focus:outline-none 
              focus:border-indigo-500">
            {LANGUAGES.map(l => (
              <option key={l} value={l}>{l}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm text-gray-400 mb-1">
            Code
          </label>
          <textarea
            ref={editorRef}
            defaultValue={initialCode}
            className="w-full"
          />
        </div>
        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            id="isPublic"
            name="isPublic"
            checked={form.isPublic}
            onChange={handleChange}
            className="rounded"
          />
          <label htmlFor="isPublic" className="text-sm text-gray-300">
            Make this snippet public
          </label>
        </div>
        <div className="flex gap-3">
          <button
            type="submit"
            disabled={saving}
            className="bg-indigo-600 hover:bg-indigo-500 
              disabled:opacity-50 text-white font-medium 
              px-6 py-2.5 rounded-lg transition-colors">
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/my-snippets')}
            className="bg-gray-700 hover:bg-gray-600 text-white 
              font-medium px-6 py-2.5 rounded-lg transition-colors">
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
