import axios from 'axios'
import router from "@/router";

// Create a configured Axios client.
const request = axios.create({
    baseURL: process.env.VUE_APP_BASEURL || 'http://localhost:9090',
    timeout: 30000                          // 30-second request timeout.
})

// Request interceptor.
// Process requests before they are sent.
// For example, attach the token or encrypt request parameters consistently.
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';        // Set the request content type.
    let user = JSON.parse(localStorage.getItem("xm-user") || '{}')  // Load the cached account.
    config.headers['token'] = user.token  // Attach the authentication header.

    return config
}, error => {
    console.error('request error: ' + error) // for debug
    return Promise.reject(error)
});

// Response interceptor.
// Process API responses consistently.
request.interceptors.response.use(
    response => {
        let res = response.data;

        // Support string responses returned by the server.
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        if (res.code === '401') {
            router.push('/login')
        }
        return res;
    },
    error => {
        console.error('response error: ' + error) // for debug
        return Promise.reject(error)
    }
)


export default request
