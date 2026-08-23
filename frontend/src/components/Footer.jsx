import { Link } from 'react-router-dom'

export default function Footer() {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="bg-gray-900 border-t border-gray-800 text-gray-400 py-8 px-6 mt-16">
      <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6">
        <div>
          <Link to="/home" className="text-lg font-bold text-indigo-400 hover:text-indigo-300 transition-colors">
            CodeShare
          </Link>
          <p className="text-xs text-gray-500 mt-1.5">
            Share code snippets, explore community contributions, and collaborate.
          </p>
        </div>

        <div className="flex flex-wrap gap-x-6 gap-y-2 text-xs font-medium">
          <Link to="/home" className="hover:text-white transition-colors">
            Browse
          </Link>
          <Link to="/search" className="hover:text-white transition-colors">
            Search
          </Link>
          <Link to="/starred" className="hover:text-white transition-colors">
            Starred
          </Link>
          <Link to="/my-snippets" className="hover:text-white transition-colors">
            Dashboard
          </Link>
        </div>

        <div className="text-[11px] text-gray-650 flex flex-col items-center md:items-end gap-1">
          <p>&copy; {currentYear} CodeShare. All rights reserved.</p>
        </div>
      </div>
    </footer>
  )
}
