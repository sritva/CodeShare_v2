import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../api'

export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/auth/login', { username, password })
      login(res.data.token, res.data.username)
      navigate('/home')
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex items-center justify-center min-h-[80vh]">
      <div className="bg-gray-900 border border-gray-700 rounded-xl 
        p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-white">
          Sign in to CodeShare
        </h1>
        {error && (
          <div className="bg-red-900/40 border border-red-700 text-red-300 
            rounded-lg px-4 py-3 mb-4 text-sm">
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
              value={username}
              onChange={e => setUsername(e.target.value)}
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
              value={password}
              onChange={e => setPassword(e.target.value)}
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
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className="text-sm text-gray-500 mt-4 text-center">
          No account?{' '}
          <Link to="/register"
            className="text-indigo-400 hover:text-indigo-300">
            Register
          </Link>
        </p>
      </div>
    </div>
  )
}
