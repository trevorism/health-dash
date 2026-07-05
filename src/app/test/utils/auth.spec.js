import { describe, it, expect, beforeEach } from 'vitest'
import { getCookieValue, getCurrentUserName, isLoggedIn } from '../../src/utils/auth'

function clearCookies() {
  document.cookie.split(';').forEach((c) => {
    const name = c.split('=')[0].trim()
    if (name) document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT`
  })
}

describe('getCookieValue', () => {
  beforeEach(clearCookies)

  it('returns the value of a present cookie', () => {
    document.cookie = 'user_name=alice'
    expect(getCookieValue('user_name')).toBe('alice')
  })

  it('returns empty string when the cookie is absent', () => {
    document.cookie = 'other=x'
    expect(getCookieValue('user_name')).toBe('')
  })

  it('returns empty string when there are no cookies at all', () => {
    expect(getCookieValue('user_name')).toBe('')
  })

  it('picks the right cookie when several are present', () => {
    document.cookie = 'a=1'
    document.cookie = 'user_name=bob'
    document.cookie = 'z=9'
    expect(getCookieValue('user_name')).toBe('bob')
  })

  it('URL-decodes the cookie value', () => {
    document.cookie = `user_name=${encodeURIComponent('bob smith@x.com')}`
    expect(getCookieValue('user_name')).toBe('bob smith@x.com')
  })

  it('returns empty string when the value cannot be decoded', () => {
    // %E0%A4%A is a truncated escape sequence that makes decodeURIComponent throw.
    document.cookie = 'user_name=%E0%A4%A'
    expect(getCookieValue('user_name')).toBe('')
  })

  it('does not match a cookie whose name is a prefix of the requested one', () => {
    document.cookie = 'user=nope'
    expect(getCookieValue('user_name')).toBe('')
  })
})

describe('getCurrentUserName', () => {
  beforeEach(clearCookies)

  it('reads the user_name cookie', () => {
    document.cookie = 'user_name=carol'
    expect(getCurrentUserName()).toBe('carol')
  })

  it('returns empty string when not set', () => {
    expect(getCurrentUserName()).toBe('')
  })
})

describe('isLoggedIn', () => {
  beforeEach(clearCookies)

  it('is true when a non-blank user_name is present', () => {
    document.cookie = 'user_name=dave'
    expect(isLoggedIn()).toBe(true)
  })

  it('is false when user_name is absent', () => {
    expect(isLoggedIn()).toBe(false)
  })

  it('is false when user_name is only whitespace', () => {
    document.cookie = `user_name=${encodeURIComponent('   ')}`
    expect(isLoggedIn()).toBe(false)
  })
})
