import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from "@tailwindcss/vite";
import fs from 'fs'
import axios from 'axios'

function readSecrets(path) {
    const out = {}
    try {
        for (const line of fs.readFileSync(path, 'utf8').split(/\r?\n/)) {
            const trimmed = line.trim()
            if (!trimmed || trimmed.startsWith('#')) continue
            const eq = trimmed.indexOf('=')
            if (eq === -1) continue
            out[trimmed.slice(0, eq).trim()] = trimmed.slice(eq + 1).trim()
        }
    } catch (e) {
        console.log('[local-auth] could not read secrets.properties:', e.message)
    }
    return out
}

let token = ''
const secrets = readSecrets('../main/resources/secrets.properties')
if (secrets.localUser && secrets.localPassword) {
    axios
        .post('https://auth.trevorism.com/token', {
            id: secrets.localUser,
            password: secrets.localPassword,
            type: 'user'
        })
        .then((response) => {
            token = response.data
            console.log('[local-auth] token acquired for local dev')
        })
        .catch((e) => {
            console.log('[local-auth] token request failed:', e.message)
        })
} else {
    console.log('[local-auth] no localUser/localPassword in secrets.properties — local login disabled')
}

export default defineConfig({
    plugins: [vue(), tailwindcss()],
    server: {
        host: 'localhost',
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:8080/',
                changeOrigin: true,
                secure: false,
                configure: (proxy, _options) => {
                    proxy.on('error', (err, _req, _res) => {
                        console.log('proxy error', err)
                    })
                    proxy.on('proxyReq', (proxyReq, req, _res) => {
                        proxyReq.setHeader('Cookie', `session=${token}`)
                    })
                    proxy.on('proxyRes', (proxyRes, req, _res) => {
                        const expires = new Date(new Date().getTime() + 60 * 15 * 1000).toUTCString()
                        _res.setHeader('Set-Cookie', [
                            `user_name=test; Path=/; Expires=${expires}`,
                            `session=${token}; Path=/; Expires=${expires}`
                        ])
                    })
                }
            }
        }
    }
})

