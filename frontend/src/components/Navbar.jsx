import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { isLoggedIn, username, logout } = useAuth()
  const navigate = useNavigate()

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
        {isLoggedIn ? (
          <>
            <Link to="/my-snippets" 
              className="text-sm text-gray-300 hover:text-white">
              My Snippets
            </Link>
            <Link to="/create" 
              className="text-sm bg-indigo-600 hover:bg-indigo-500 
                px-3 py-1.5 rounded-md">
              + New
            </Link>
            <span className="text-sm text-gray-400">
              {username}
            </span>
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
