import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../api'

export default function UserProfile() {
  const { username: profileUsername } = useParams()
  const { username: loggedInUsername, isLoggedIn } = useAuth()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Edit states
  const [isEditing, setIsEditing] = useState(false)
  const [bio, setBio] = useState('')
  const [avatarUrl, setAvatarUrl] = useState('')
  const [githubUrl, setGithubUrl] = useState('')
  const [linkedinUrl, setLinkedinUrl] = useState('')
  const [saveLoading, setSaveLoading] = useState(false)
  const [saveError, setSaveError] = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')
    api.get(`/users/${profileUsername}`)
      .then(res => {
        setProfile(res.data)
        setBio(res.data.bio || '')
        setAvatarUrl(res.data.avatarUrl || '')
        setGithubUrl(res.data.githubUrl || '')
        setLinkedinUrl(res.data.linkedinUrl || '')
      })
      .catch(err => {
        setError(err.response?.data?.message || 'User profile not found')
      })
      .finally(() => setLoading(false))
  }, [profileUsername])

  const handleSave = async (e) => {
    e.preventDefault()
    setSaveLoading(true)
    setSaveError('')
    try {
      const res = await api.put('/users/profile', {
        bio,
        avatarUrl,
        githubUrl,
        linkedinUrl
      })
      setProfile(prev => ({
        ...prev,
        bio: res.data.bio,
        avatarUrl: res.data.avatarUrl,
        githubUrl: res.data.githubUrl,
        linkedinUrl: res.data.linkedinUrl
      }))
      setIsEditing(false)
    } catch (err) {
      setSaveError('Failed to update profile')
    } finally {
      setSaveLoading(false)
    }
  }

  const isSelf = isLoggedIn && loggedInUsername === profileUsername

  if (loading) {
    return (
      <div className="text-center text-gray-500 py-16">
        Loading profile...
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center">
        <p className="text-red-400 mb-4">{error}</p>
        <Link to="/home" className="text-indigo-400 hover:text-indigo-300 text-sm">
          Back to home
        </Link>
      </div>
    )
  }

  const defaultAvatar = `https://api.dicebear.com/7.x/bottts/svg?seed=${profileUsername}`

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* Profile Card */}
      <div className="bg-gray-900 border border-gray-700 rounded-xl p-6 mb-8 shadow-xl">
        <div className="flex flex-col md:flex-row gap-6 items-center md:items-start">
          <img
            src={profile.avatarUrl || defaultAvatar}
            alt={`${profileUsername}'s avatar`}
            className="w-24 h-24 rounded-full border border-gray-600 bg-gray-800 object-cover shrink-0"
            onError={(e) => { e.target.src = defaultAvatar }}
          />

          <div className="flex-1 text-center md:text-left">
            <div className="flex flex-col md:flex-row items-center md:items-start justify-between gap-4 mb-2">
              <h1 className="text-2xl font-bold text-white">@{profileUsername}</h1>
              {isSelf && !isEditing && (
                <button
                  onClick={() => setIsEditing(true)}
                  className="text-xs bg-gray-800 hover:bg-gray-700 border border-gray-700 text-gray-300 px-3 py-1.5 rounded-lg transition-colors font-medium"
                >
                  Edit Profile
                </button>
              )}
            </div>

            {isEditing ? (
              <form onSubmit={handleSave} className="space-y-4 mt-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-400 mb-1">Biography</label>
                  <textarea
                    value={bio}
                    onChange={e => setBio(e.target.value)}
                    placeholder="Tell us about yourself..."
                    maxLength={1000}
                    rows={3}
                    className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500 placeholder-gray-600"
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-400 mb-1">Avatar Image URL</label>
                    <input
                      type="url"
                      value={avatarUrl}
                      onChange={e => setAvatarUrl(e.target.value)}
                      placeholder="https://example.com/avatar.jpg"
                      className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500 placeholder-gray-600"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-400 mb-1">GitHub Profile Link</label>
                    <input
                      type="url"
                      value={githubUrl}
                      onChange={e => setGithubUrl(e.target.value)}
                      placeholder="https://github.com/username"
                      className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500 placeholder-gray-600"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-400 mb-1">LinkedIn Profile Link</label>
                    <input
                      type="url"
                      value={linkedinUrl}
                      onChange={e => setLinkedinUrl(e.target.value)}
                      placeholder="https://linkedin.com/in/username"
                      className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500 placeholder-gray-600"
                    />
                  </div>
                </div>

                {saveError && <p className="text-red-400 text-xs">{saveError}</p>}

                <div className="flex justify-end gap-2 pt-2">
                  <button
                    type="button"
                    onClick={() => setIsEditing(false)}
                    className="text-xs bg-gray-850 hover:bg-gray-800 border border-gray-700 text-gray-400 px-3 py-1.5 rounded-lg transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={saveLoading}
                    className="text-xs bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white px-3 py-1.5 rounded-lg transition-colors font-semibold"
                  >
                    {saveLoading ? 'Saving...' : 'Save Changes'}
                  </button>
                </div>
              </form>
            ) : (
              <div>
                <p className="text-gray-300 text-sm whitespace-pre-wrap leading-relaxed mb-4">
                  {profile.bio || "No biography provided yet."}
                </p>

                {/* Social Badges */}
                <div className="flex flex-wrap gap-3">
                  {profile.githubUrl && (
                    <a
                      href={profile.githubUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1.5 px-3 py-1 bg-gray-800 hover:bg-gray-750 text-gray-300 text-xs rounded-full border border-gray-700 transition-colors"
                    >
                      <span className="font-semibold text-xs">🐙</span> GitHub
                    </a>
                  )}
                  {profile.linkedinUrl && (
                    <a
                      href={profile.linkedinUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center gap-1.5 px-3 py-1 bg-gray-800 hover:bg-gray-750 text-gray-300 text-xs rounded-full border border-gray-700 transition-colors"
                    >
                      <span className="font-semibold text-xs">🔗</span> LinkedIn
                    </a>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Snippets Section */}
      <div>
        <h2 className="text-lg font-bold text-white mb-4">
          Public Snippets ({profile.snippets.length})
        </h2>

        {profile.snippets.length === 0 ? (
          <div className="text-center py-12 bg-gray-900 border border-gray-700 rounded-xl">
            <p className="text-gray-500 text-sm">No public snippets shared yet.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {profile.snippets.map(s => (
              <div
                key={s.id}
                className="bg-gray-900 border border-gray-700 hover:border-gray-600 rounded-xl p-5 transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-bold text-white text-base truncate pr-2">
                      <Link to={`/snippet/${s.id}`} className="hover:text-indigo-400 transition-colors">
                        {s.title}
                      </Link>
                    </h3>
                    <span className="font-mono bg-gray-850 px-2 py-0.5 rounded text-[10px] text-gray-400 border border-gray-800 uppercase font-semibold">
                      {s.language}
                    </span>
                  </div>

                  {s.parentIdValue && (
                    <div className="text-[10px] text-gray-500 mb-3">
                      🍴 Forked from{' '}
                      <Link
                        to={`/snippet/${s.parentIdValue}`}
                        className="text-indigo-400 hover:underline"
                      >
                        @{s.parentUsername}/{s.parentTitle}
                      </Link>
                    </div>
                  )}

                  <pre className="code-preview-box text-xs font-mono border rounded p-2.5 mb-4 line-clamp-3 overflow-hidden leading-relaxed whitespace-pre-wrap">
                    {s.code}
                  </pre>
                </div>

                <div className="flex items-center justify-between border-t border-gray-850 pt-3 text-[11px] text-gray-500">
                  <span>{new Date(s.createdAt).toLocaleDateString()}</span>
                  <div className="flex items-center gap-1">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 24 24" className="w-3.5 h-3.5 text-rose-500">
                      <path d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3c1.549 0 2.94.746 3.812 1.914.87-1.168 2.263-1.914 3.812-1.914 2.975 0 5.438 2.322 5.438 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z" />
                    </svg>
                    <span>{s.likesCount || 0}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
