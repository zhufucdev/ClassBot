import * as sha1 from 'js-sha1'

export default function(req, res) {
    const origin = req.query.origin
    if (typeof origin === 'undefined') {
        res.status(400).send("No origin provided")
        return
    }

    const redirect = global.redirection[origin]
    const key = sha1(origin)
    if (typeof global.identity === 'undefined') {
        global.identity = new Object()
    }
    global.identity[key] = {
        classroom: redirect.classroom,
        student: redirect.student
    }
    
    res.writeHead(302, {
        'Set-Cookie': 'key=' + key,
        'Location': redirect.target
    })
    res.end()
}
