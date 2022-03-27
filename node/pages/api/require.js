import { v4 as uuid } from 'uuid'

const localhost = /(localhost|127\.0\.0\.1|::1):[0-9]*/g

export default function(req, res) {
    if (!localhost.test(req.headers.host)) {
        res.status(503).send("Not accessing from local loopback: " + req.headers.host)
        return
    }

    const classroom = req.query.classroom,
     student = req.query.student,
     target = req.query.target;
    if (typeof classroom === 'undefined' || typeof student === 'undefined' || typeof target === 'undefined') {
        res.status(400).send("Not all parameters were provided")
        return
    }

    const origin = uuid()
    const redirect = {
        target: target,
        classroom: classroom,
        student: student
    }

    if (typeof global.redirection === 'undefined') {
        global.redirection = new Object()
    }
    global.redirection[origin] = redirect

    res.status(200).send("/api/redirect?origin=" + origin)
}
