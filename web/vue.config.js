const { defineConfig } = require('@vue/cli-service')
const baseUrl = 'http://127.0.0.1:8080'; // 提取 baseUrl

module.exports = defineConfig({
    devServer: {
        port: 8081,
        host: '0.0.0.0', // 修改主机地址
        client: {
            webSocketURL: 'ws://0.0.0.0:8081/ws', // 修改WebSocket URL
        },
       
        proxy: {
            '/orders': {
                target: `${baseUrl}/`,
                changeOrigin: true,
                onProxyReq(proxyReq) {
                    proxyReq.removeHeader('origin')
                }
            },
            '/login': {
                target: `${baseUrl}/`,
                changeOrigin: true,
                secure: false,
                onProxyReq(proxyReq) {
                    proxyReq.removeHeader('origin')
                }
            },
            '/buttonAction': {
                target: `${baseUrl}/`,
                changeOrigin: true,
                secure: false,
            },
            '/setAccount': {
                target: `${baseUrl}/`,
                changeOrigin: true,
                secure: false,
            }, 
            '/machines': {
                target: `${baseUrl}/`,
                changeOrigin: true,
                secure: false,
            },
            '/ws': {
                target: `${baseUrl}/`,
                ws: true,
                changeOrigin: true,
                secure: false,
                logLevel: 'debug'
            },
            '/machines': {
                target: `${baseUrl}/`,
                changeOrigin: true,
                secure: false,
            },
        }
    }
})
