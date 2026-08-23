import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ReactMarkdown from 'react-markdown'
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

export default function ViewSnippet() {
  const { id } = useParams()
  const { username, isLoggedIn } = useAuth()
  const navigate = useNavigate()
  const [snippet, setSnippet] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [explanation, setExplanation] = useState('')
  const [explaining, setExplaining] = useState(false)
  const [explainError, setExplainError] = useState('')
  const [copied, setCopied] = useState(false)
  const [shareToken, setShareToken] = useState(null)
  const [shareEnabled, setShareEnabled] = useState(false)
  const [shareLoading, setShareLoading] = useState(false)
  const [shareCopied, setShareCopied] = useState(false)
  const [shareError, setShareError] = useState('')
  const [showAiExplanation, setShowAiExplanation] = useState(true)

  // Likes & Comments State
  const [likesCount, setLikesCount] = useState(0)
  const [liked, setLiked] = useState(false)
  const [likeLoading, setLikeLoading] = useState(false)
  const [comments, setComments] = useState([])
  const [commentsLoading, setCommentsLoading] = useState(false)
  const [newComment, setNewComment] = useState('')
  const [commentError, setCommentError] = useState('')
  const [commentSubmitting, setCommentSubmitting] = useState(false)
  const [forkLoading, setForkLoading] = useState(false)
  const [starsCount, setStarsCount] = useState(0)
  const [starred, setStarred] = useState(false)
  const [starLoading, setStarLoading] = useState(false)

  const editorRef = useRef(null)
  const cmRef = useRef(null)

  useEffect(() => {
    api.get(`/snippets/${id}`)
      .then(res => {
        setSnippet(res.data)
        setLikesCount(res.data.likesCount || 0)
        setLiked(res.data.liked || false)
        setStarsCount(res.data.starsCount || 0)
        setStarred(res.data.starred || false)
        if (res.data.aiExplanation) {
          setExplanation(res.data.aiExplanation)
        }
        if (res.data.shareToken && res.data.shareEnabled) {
          setShareToken(res.data.shareToken)
          setShareEnabled(res.data.shareEnabled)
        }
      })
      .catch(() => setError('Snippet not found or access denied'))
      .finally(() => setLoading(false))

    // Load comments
    setCommentsLoading(true)
    api.get(`/snippets/${id}/comments`)
      .then(res => setComments(res.data))
      .catch(() => {})
      .finally(() => setCommentsLoading(false))
  }, [id])

  useEffect(() => {
    if (snippet && editorRef.current && !cmRef.current) {
      cmRef.current = CodeMirror.fromTextArea(editorRef.current, {
        value: snippet.code,
        theme: 'material-darker',
        lineNumbers: true,
        lineWrapping: true,
        readOnly: 'nocursor',
        mode: CM_MODES[snippet.language] || null
      })
      cmRef.current.setSize(null, 'auto')
    }
    return () => {
      if (cmRef.current) {
        cmRef.current.toTextArea()
        cmRef.current = null
      }
    }
  }, [snippet])

  const handleExplain = async () => {
    setExplaining(true)
    setExplainError('')
    setShowAiExplanation(true)
    try {
      const res = await api.post(`/snippets/${id}/explain`)
      setExplanation(res.data.explanation)
    } catch (err) {
      setExplainError(
        err.response?.data?.error || 'Failed to generate explanation'
      )
    } finally {
      setExplaining(false)
    }
  }

  const handleCopy = () => {
    navigator.clipboard.writeText(snippet.code)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  const handleLikeToggle = async () => {
    if (!isLoggedIn) {
      navigate('/login')
      return
    }
    setLikeLoading(true)
    try {
      if (liked) {
        await api.post(`/snippets/${id}/unlike`)
        setLiked(false)
        setLikesCount(prev => Math.max(0, prev - 1))
      } else {
        await api.post(`/snippets/${id}/like`)
        setLiked(true)
        setLikesCount(prev => prev + 1)
      }
    } catch (err) {
      // ignore
    } finally {
      setLikeLoading(false)
    }
  }

  const handleStarToggle = async () => {
    if (!isLoggedIn) {
      navigate('/login')
      return
    }
    setStarLoading(true)
    try {
      if (starred) {
        await api.post(`/snippets/${id}/unstar`)
        setStarred(false)
        setStarsCount(prev => Math.max(0, prev - 1))
      } else {
        await api.post(`/snippets/${id}/star`)
        setStarred(true)
        setStarsCount(prev => prev + 1)
      }
    } catch (err) {
      // ignore
    } finally {
      setStarLoading(false)
    }
  }

  const handleFork = async () => {
    if (!isLoggedIn) {
      navigate('/login')
      return
    }
    if (!window.confirm('Would you like to fork this snippet to your profile?')) return
    setForkLoading(true)
    try {
      const res = await api.post(`/snippets/${id}/fork`)
      navigate(`/snippet/${res.data.id}`)
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to fork snippet')
    } finally {
      setForkLoading(false)
    }
  }

  const handleAddComment = async (e) => {
    e.preventDefault()
    if (!newComment.trim()) return
    setCommentSubmitting(true)
    setCommentError('')
    try {
      const res = await api.post(`/snippets/${id}/comments`, { content: newComment })
      setComments(prev => [...prev, res.data])
      setNewComment('')
    } catch (err) {
      setCommentError(err.response?.data?.error || 'Failed to post comment')
    } finally {
      setCommentSubmitting(false)
    }
  }

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('Are you sure you want to delete this comment?')) return
    try {
      await api.delete(`/snippets/comments/${commentId}`)
      setComments(prev => prev.filter(c => c.id !== commentId))
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to delete comment')
    }
  }

  const handleShare = async () => {
    setShareLoading(true)
    setShareError('')
    try {
      const res = await api.post(`/snippets/${id}/share`)
      setShareToken(res.data.shareToken)
      setShareEnabled(true)
    } catch (err) {
      setShareError(
        err.response?.data?.error || 'Failed to generate share link'
      )
    } finally {
      setShareLoading(false)
    }
  }

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this snippet?')) {
      return
    }
    try {
      await api.delete(`/snippets/${id}`)
      navigate('/my-snippets')
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to delete snippet')
    }
  }

  const handleUnshare = async () => {
    setShareLoading(true)
    try {
      await api.post(`/snippets/${id}/unshare`)
      setShareEnabled(false)
    } catch (err) {
      setShareError('Failed to disable sharing')
    } finally {
      setShareLoading(false)
    }
  }

  const handleCopyShareLink = () => {
    const url = `${window.location.origin}/share/snippet/${shareToken}`
    navigator.clipboard.writeText(url)
    setShareCopied(true)
    setTimeout(() => setShareCopied(false), 2000)
  }

  const isOwner = isLoggedIn && snippet?.username === username

  if (loading) {
    return (
      <div className="text-center text-gray-500 py-16">
        Loading snippet...
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center">
        <p className="text-red-400 mb-4">{error}</p>
        <Link to="/home"
          className="text-indigo-400 hover:text-indigo-300 text-sm">
          Back to home
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">

      {/* Header */}
      <div className="flex items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white mb-2">
            {snippet.title}
          </h1>
          {snippet.parentId && (
            <div className="text-xs text-gray-500 mb-2">
              🍴 Forked from{' '}
              <Link to={`/snippet/${snippet.parentId}`} className="text-indigo-400 hover:underline">
                @{snippet.parentUsername}/{snippet.parentTitle}
              </Link>
            </div>
          )}
          <div className="flex flex-wrap items-center gap-3 text-sm text-gray-400">
            <span>by <Link to={`/user/${snippet.username}`} className="text-indigo-400 hover:text-indigo-300 font-medium">@{snippet.username}</Link></span>
            <span>{"\u00B7"}</span>
            <span className="font-mono bg-gray-800 px-2 py-0.5 rounded text-xs">
              {snippet.language}
            </span>
            <span>{"\u00B7"}</span>
            <span>
              {new Date(snippet.createdAt).toLocaleDateString()}
            </span>
            <span className={`px-2 py-0.5 rounded text-xs
              ${snippet.public
                ? 'bg-green-900/40 text-green-400'
                : 'bg-gray-800 text-gray-500'}`}>
              {snippet.public ? 'Public' : 'Private'}
            </span>
            <span>{"\u00B7"}</span>
            <button
              onClick={handleLikeToggle}
              disabled={likeLoading}
              className={`flex items-center gap-1.5 px-3 py-0.5 rounded-full text-xs font-semibold border transition-all duration-200
                ${liked 
                  ? 'bg-rose-950/40 border-rose-800/85 text-rose-400 hover:bg-rose-900/40' 
                  : 'bg-gray-800 border-gray-700 text-gray-400 hover:text-rose-400 hover:border-rose-800/50'}`}
              title={liked ? "Unlike snippet" : "Like snippet"}
            >
              {liked ? (
                <svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 24 24" className="w-3.5 h-3.5 text-rose-400">
                  <path d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3c1.549 0 2.94.746 3.812 1.914.87-1.168 2.263-1.914 3.812-1.914 2.975 0 5.438 2.322 5.438 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z" />
                </svg>
              ) : (
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-3.5 h-3.5">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z" />
                </svg>
              )}
              <span>{likesCount}</span>
            </button>
            <button
              onClick={handleStarToggle}
              disabled={starLoading}
              className={`flex items-center gap-1.5 px-3 py-0.5 rounded-full text-xs font-semibold border transition-all duration-200
                ${starred 
                  ? 'bg-amber-950/40 border-amber-800/85 text-amber-400 hover:bg-amber-900/40' 
                  : 'bg-gray-800 border-gray-700 text-gray-400 hover:text-amber-400 hover:border-amber-800/50'}`}
              title={starred ? "Unstar snippet" : "Star snippet"}
            >
              {starred ? (
                <svg xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 24 24" className="w-3.5 h-3.5 text-amber-400">
                  <path fillRule="evenodd" d="M10.788 3.21c.448-1.077 1.976-1.077 2.424 0l2.082 5.007 5.404.433c1.164.093 1.636 1.545.749 2.305l-4.117 3.527 1.257 5.273c.271 1.136-.964 2.033-1.96 1.425L12 18.354 7.373 21.18c-.996.608-2.231-.29-1.96-1.425l1.257-5.273-4.117-3.527c-.887-.76-.415-2.212.749-2.305l5.404-.433 2.082-5.006z" clipRule="evenodd" />
                </svg>
              ) : (
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-3.5 h-3.5">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M11.48 3.499c.195-.39.73-.39.925 0l2.135 4.267 4.707.684c.43.063.602.585.29.896l-3.405 3.32 1.057 4.693c.097.43-.356.76-.738.556l-4.204-2.21-4.204 2.21c-.382.204-.835-.126-.738-.556l1.057-4.693-3.405-3.32c-.312-.311-.14-.833.29-.896l4.707-.684 2.135-4.267z" />
                </svg>
              )}
              <span>{starsCount}</span>
            </button>
          </div>
        </div>
        <div className="flex gap-2 shrink-0">
          {!isOwner && (
            <button
              onClick={handleFork}
              disabled={forkLoading}
              className="text-sm bg-indigo-650 hover:bg-indigo-600 disabled:opacity-50
                text-white px-3 py-1.5 rounded-lg transition-all font-medium shadow-md hover:shadow-indigo-900/40"
            >
              {forkLoading ? 'Forking...' : 'Fork'}
            </button>
          )}
          {isOwner && (
            <>
              <Link
                to={`/edit/${snippet.id}`}
                className="text-sm bg-gray-800 hover:bg-gray-700 
                  text-gray-300 px-3 py-1.5 rounded-lg transition-colors font-medium">
                Edit
              </Link>
              <button
                onClick={handleDelete}
                className="text-sm bg-red-950/40 hover:bg-red-900/60 
                  text-red-400 border border-red-800/60 px-3 py-1.5 rounded-lg transition-colors font-medium">
                Delete
              </button>
            </>
          )}
        </div>
      </div>

      {/* Code block */}
      <div className="relative bg-gray-900 border border-gray-700 
        rounded-xl overflow-hidden mb-6 cm-read-only">
        <div className="flex items-center justify-between px-4 py-2 
          border-b border-gray-700 bg-gray-800">
          <span className="text-xs text-gray-400 font-mono">
            {snippet.language}
          </span>
          <div className="relative">
            <button
              onClick={handleCopy}
              className="text-xs text-gray-400 hover:text-white 
                transition-colors">
              {copied ? '✓ Copied' : 'Copy'}
            </button>
            {copied && (
              <span className="absolute bottom-full right-0 mb-2 px-2 py-1 
                bg-indigo-600 text-white text-xs rounded shadow-lg border border-indigo-500 
                whitespace-nowrap animate-bounce">
                Copied to clipboard!
              </span>
            )}
          </div>
        </div>
        <div className="p-4 bg-gray-900">
          <textarea
            ref={editorRef}
            defaultValue={snippet.code}
          />
        </div>
      </div>

      {/* Share section */}
      {isLoggedIn && (snippet?.public || isOwner) && (
        <div className="bg-gray-900 border border-gray-700 
          rounded-xl p-5 mb-6">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-sm font-medium text-gray-300">
              Share Snippet
            </h2>
            {!shareEnabled ? (
              <button
                onClick={handleShare}
                disabled={shareLoading}
                className="text-xs bg-gray-700 hover:bg-gray-600 
                  disabled:opacity-50 text-white px-3 py-1.5 
                  rounded-lg transition-colors">
                {shareLoading ? 'Generating...' : 'Generate share link'}
              </button>
            ) : (
              <button
                onClick={handleUnshare}
                disabled={shareLoading}
                className="text-xs bg-red-900/30 hover:bg-red-900/50 
                  disabled:opacity-50 text-red-400 px-3 py-1.5 
                  rounded-lg transition-colors">
                {shareLoading ? 'Disabling...' : 'Disable sharing'}
              </button>
            )}
          </div>

          {shareError && (
            <p className="text-red-400 text-xs mb-2">{shareError}</p>
          )}

          {shareEnabled && shareToken && (
            <div className="flex items-center gap-2">
              <code className="flex-1 bg-gray-800 text-gray-300 text-xs 
                px-3 py-2 rounded-lg truncate font-mono">
                {`${window.location.origin}/share/snippet/${shareToken}`}
              </code>
              <div className="relative shrink-0">
                <button
                  onClick={handleCopyShareLink}
                  className="text-xs bg-indigo-600 
                    hover:bg-indigo-500 text-white px-3 py-2 
                    rounded-lg transition-colors">
                  {shareCopied ? '✓ Copied' : 'Copy'}
                </button>
                {shareCopied && (
                  <span className="absolute bottom-full right-0 mb-2 px-2 py-1 
                    bg-indigo-600 text-white text-xs rounded shadow-lg border border-indigo-500 
                    whitespace-nowrap animate-bounce">
                    Link Copied!
                  </span>
                )}
              </div>
            </div>
          )}

          {!shareEnabled && !shareToken && (
            <p className="text-gray-600 text-xs">
              Generate a link to share this snippet with anyone,
              even without an account.
            </p>
          )}
        </div>
      )}

      {/* AI Explanation Accordion */}
      <div className="bg-gray-900 border border-gray-700 rounded-xl p-5 mb-6">
        <div className="flex items-center justify-between">
          <div 
            onClick={() => setShowAiExplanation(prev => !prev)}
            className="flex items-center gap-2 cursor-pointer select-none group"
          >
            <span className={`text-xs text-gray-400 group-hover:text-white transition-transform duration-200 ${showAiExplanation ? 'rotate-90' : 'rotate-0'}`}>
              ▶
            </span>
            <h2 className="text-sm font-medium text-gray-300 group-hover:text-white transition-colors">
              AI Explanation
            </h2>
          </div>
          <div className="flex items-center gap-3">
            {isLoggedIn && (
              <button
                onClick={handleExplain}
                disabled={explaining}
                className="text-xs bg-indigo-600 hover:bg-indigo-500 
                  disabled:opacity-50 text-white px-3 py-1.5 
                  rounded-lg transition-colors">
                {explaining
                  ? 'Generating...'
                  : explanation
                    ? 'Regenerate'
                    : 'Explain this code'}
              </button>
            )}
            {!isLoggedIn && (
              <Link to="/login"
                className="text-xs text-indigo-400 hover:text-indigo-300 font-medium">
                Sign in to use AI explanation
              </Link>
            )}
          </div>
        </div>

        {/* Accordion Content with Height/Opacity transition */}
        <div className={`transition-all duration-300 overflow-hidden ${showAiExplanation ? 'mt-4 max-h-[1500px] opacity-100' : 'max-h-0 opacity-0'}`}>
          {explainError && (
            <p className="text-red-400 text-sm">{explainError}</p>
          )}

          {explaining && (
            <p className="text-gray-500 text-sm">
              Analyzing your code...
            </p>
          )}

          {explanation && !explaining && (
            <div className="text-gray-300 text-sm leading-relaxed 
              prose prose-invert prose-sm max-w-none border-t border-gray-800 pt-4">
              <ReactMarkdown
                components={{
                  code: ({node, className, children, ...props}) => {
                    const match = /language-(\w+)/.exec(className || '')
                    return !match ? (
                      <code className="bg-gray-800 text-indigo-300 px-1.5 py-0.5 rounded text-xs font-mono" {...props}>
                        {children}
                      </code>
                    ) : (
                      <pre className="bg-gray-800 rounded-lg p-3 overflow-x-auto my-2">
                        <code className={`${className} text-xs font-mono text-gray-300`} {...props}>
                          {children}
                        </code>
                      </pre>
                    )
                  },
                  strong: ({children}) => (
                    <strong className="text-white font-semibold">
                      {children}
                    </strong>
                  ),
                  p: ({children}) => (
                    <div className="mb-2 last:mb-0">{children}</div>
                  ),
                  ul: ({children}) => (
                    <ul className="list-disc list-inside space-y-1 mb-2">
                      {children}
                    </ul>
                  ),
                  ol: ({children}) => (
                    <ol className="list-decimal list-inside space-y-1 mb-2">
                      {children}
                    </ol>
                  ),
                  li: ({children}) => (
                    <li className="text-gray-300">{children}</li>
                  ),
                }}
              >
                {explanation}
              </ReactMarkdown>
            </div>
          )}

          {!explanation && !explaining && !explainError && (
            <p className="text-gray-600 text-sm border-t border-gray-800 pt-4">
              {isLoggedIn
                ? 'Click "Explain this code" to get an AI-powered explanation.'
                : 'Sign in to get an AI-powered explanation of this snippet.'}
            </p>
          )}
        </div>
      </div>

      {/* Comments Section */}
      <div className="bg-gray-900 border border-gray-700 rounded-xl p-5 mb-6">
        <h2 className="text-sm font-medium text-gray-300 mb-4 flex items-center gap-2">
          <span>💬</span> Comments ({comments.length})
        </h2>

        {/* Comment list */}
        {commentsLoading ? (
          <p className="text-gray-500 text-xs py-2">Loading comments...</p>
        ) : comments.length === 0 ? (
          <p className="text-gray-500 text-xs py-4 border-b border-gray-800">
            No comments yet. Share your thoughts below!
          </p>
        ) : (
          <div className="space-y-4 mb-4 max-h-[300px] overflow-y-auto pr-2 border-b border-gray-800 pb-4">
            {comments.map(c => {
              const canDelete = isLoggedIn && (c.username === username || snippet.username === username)
              return (
                <div key={c.id} className="bg-gray-800/30 p-3 rounded-lg border border-gray-850 flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <Link to={`/user/${c.username}`} className="text-xs font-semibold text-white hover:text-indigo-400 transition-colors">@{c.username}</Link>
                      <span className="text-[10px] text-gray-500">
                        {new Date(c.createdAt).toLocaleString()}
                      </span>
                    </div>
                    <p className="text-xs text-gray-350 leading-relaxed whitespace-pre-wrap">{c.content}</p>
                  </div>
                  {canDelete && (
                    <button
                      onClick={() => handleDeleteComment(c.id)}
                      className="text-[10px] text-red-400 hover:text-red-300 px-1 py-0.5 rounded transition-colors"
                    >
                      Delete
                    </button>
                  )}
                </div>
              )
            })}
          </div>
        )}

        {/* Post comment form */}
        {isLoggedIn ? (
          <form onSubmit={handleAddComment} className="space-y-2 mt-4">
            <textarea
              value={newComment}
              onChange={e => setNewComment(e.target.value)}
              placeholder="Write a comment..."
              required
              maxLength={1000}
              rows={2}
              className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-white text-xs focus:outline-none focus:border-indigo-500 placeholder-gray-650"
            />
            {commentError && <p className="text-red-400 text-xs">{commentError}</p>}
            <div className="flex justify-end">
              <button
                type="submit"
                disabled={commentSubmitting || !newComment.trim()}
                className="text-xs bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white px-3 py-1.5 rounded-lg transition-colors font-medium"
              >
                {commentSubmitting ? 'Posting...' : 'Post Comment'}
              </button>
            </div>
          </form>
        ) : (
          <p className="text-gray-500 text-xs mt-4">
            <Link to="/login" className="text-indigo-400 hover:text-indigo-300 font-medium">Sign in</Link> to post comments.
          </p>
        )}
      </div>

      <div className="mt-6">
        <Link to="/home"
          className="text-sm text-gray-500 hover:text-gray-300 
            transition-colors">
          {"\u2190"} Back to snippets
        </Link>
      </div>
    </div>
  )
}
