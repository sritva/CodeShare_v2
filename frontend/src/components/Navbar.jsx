import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { isLoggedIn, username, logout } = useAuth()
  const navigate = useNavigate()

  const [isDark, setIsDark] = useState(() => {
    const saved = localStorage.getItem('theme')
    if (saved === 'light') return false
    return true
  })

  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }, [isDark])

  return (
    <nav className="bg-gray-900 text-white px-6 py-3 flex items-center 
      justify-between border-b border-gray-700">
      <Link to="/home" 
        className="text-xl font-bold text-indigo-400 hover:text-indigo-300">
        CodeShare
      </Link>
      <div className="flex items-center gap-4">
        <Link to="/home" 
          className="text-sm text-gray-300 hover:text-white">
          Browse
        </Link>
        <Link to="/search" 
          className="text-sm text-gray-300 hover:text-white">
          Search
        </Link>
        
        <button
          onClick={() => setIsDark(!isDark)}
          className="theme-toggle-btn"
          title="Toggle Theme"
          aria-label="Toggle Theme"
        >
          {isDark ? (
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#f0883e' }}>
              <circle cx="12" cy="12" r="5"></circle>
              <line x1="12" y1="1" x2="12" y2="3"></line>
              <line x1="12" y1="21" x2="12" y2="23"></line>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
              <line x1="1" y1="12" x2="3" y2="12"></line>
              <line x1="21" y1="12" x2="23" y2="12"></line>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
            </svg>
          ) : (
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ color: '#57606a' }}>
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>
          )}
        </button>

        {isLoggedIn ? (
          <>
            <Link to="/my-snippets" 
              className="text-sm text-gray-300 hover:text-white">
              My Snippets
            </Link>
            <Link to="/starred" 
              className="text-sm text-gray-300 hover:text-white">
              Starred
            </Link>
            <Link to="/create" 
              className="text-sm bg-indigo-600 hover:bg-indigo-500 
                px-3 py-1.5 rounded-md">
              + New
            </Link>
            <Link to={`/user/${username}`} 
              className="text-sm text-gray-300 hover:text-white">
              {username}
            </Link>
            <button onClick={logout} 
              className="text-sm text-gray-400 hover:text-white">
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" 
              className="text-sm text-gray-300 hover:text-white">
              Login
            </Link>
            <Link to="/register" 
              className="text-sm bg-indigo-600 hover:bg-indigo-500 
                px-3 py-1.5 rounded-md">
              Register
            </Link>
          </>
        )}
      </div>
    </nav>
  )
}
