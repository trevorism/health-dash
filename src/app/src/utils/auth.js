export function getCookieValue(name) {
  const cookiePrefix = `${name}=`
  const cookies = document.cookie ? document.cookie.split('; ') : []

  for (const cookie of cookies) {
    if (cookie.startsWith(cookiePrefix)) {
      try {
        return decodeURIComponent(cookie.substring(cookiePrefix.length))
      } catch {
        return ''
      }
    }
  }

  return ''
}

export function getCurrentUserName() {
  return getCookieValue('user_name')
}

export function isLoggedIn() {
  return !!getCurrentUserName()?.trim()
}
