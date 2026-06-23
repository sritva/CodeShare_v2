import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api'

export default function Register() {
  const [form, setForm] = useState({
    username: '', password: '', confirmPassword: ''
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleChange = e =>
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match')
      return
    }
    setLoading(true)
    try {
      await api.post('/auth/register', form)
      navigate('/login?registered=true')
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex items-center justify-center min-h-[80vh]">
      <div className="bg-gray-900 border border-gray-700 rounded-xl 
        p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-white">
          Create an account
        </h1>
        {error && (
          <div className="bg-red-900/40 border border-red-700 
            text-red-300 rounded-lg px-4 py-3 mb-4 text-sm">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm text-gray-400 mb-1">
              Username
            </label>
            <input
              type="text"
              name="username"
              value={form.username}
              onChange={handleChange}
              className="w-full bg-gray-800 border border-gray-600 
                rounded-lg px-4 py-2.5 text-white text-sm 
                focus:outline-none focus:border-indigo-500"
              required
            />
          </div>
          <div>
            <label className="block text-sm text-gray-400 mb-1">
              Password
            </label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              className="w-full bg-gray-800 border border-gray-600 
                rounded-lg px-4 py-2.5 text-white text-sm 
                focus:outline-none focus:border-indigo-500"
              required
            />
          </div>
          <div>
            <label className="block text-sm text-gray-400 mb-1">
              Confirm Password
            </label>
            <input
              type="password"
              name="confirmPassword"
              value={form.confirmPassword}
              onChange={handleChange}
              className="w-full bg-gray-800 border border-gray-600 
                rounded-lg px-4 py-2.5 text-white text-sm 
                focus:outline-none focus:border-indigo-500"
              required
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-indigo-600 hover:bg-indigo-500 
              disabled:opacity-50 text-white font-medium py-2.5 
              rounded-lg transition-colors">
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>
        <p className="text-sm text-gray-500 mt-4 text-center">
          Already have an account?{' '}
          <Link to="/login"
            className="text-indigo-400 hover:text-indigo-300">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
