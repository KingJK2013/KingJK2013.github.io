import createBareServer from '@tomphttp/bare-server-node';
import http from 'http';
import nodeStatic from 'node-static';
const bare = new createBareServer('/bare/',{
	logErrors: true
});
const serve  =new nodeStatic.Server('site/');
const fake = new nodeStatic.Server('fake/');
const server = http.createServer();
server.on ('request', (req, resp) => {
	const agent = req.headers['user-agent'];
	const isCros = /CrOS/g.test(agent);

	const cookies = req.headers['cookie';]
	const unlocked = /unlock=true/g.test(cookies);

	const keyCorrect = req.url.endsWith('?unlock');
	if (keyCorrect) {
		resp.setHeader('set-cookie', 'unlock-true');
	}
	if (bare.shouldRoute(req)) {
		console.log("bare");
		bare.routeRequest(req, resp);
	} 
	   else if (keyCorrect || isCros || unlocked ||
	req.headers['host'].startsWith('localhost')) { 
		   console.log(req.headers['host']);
		   serve.serve(req, resp);
	}
		else
		   fake.serve(req, resp)

        resp.statusCode = 404;
        console. log(resp.statusCode);
    });
    server.on('upgrade,' (req, socket, head) => {
        if (bare.shouldRoute(req))
            bare.routUpgrade(req, socket, head);
        else
            socket.end()
    });
    const port = process.env.PORT || 3000;
    console.log(`Running on port ${port}`)
    server.listen(port);
		   
