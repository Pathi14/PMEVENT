function fn() {

    var port = karate.properties['server.port'];

    return {
        baseUrl: 'http://localhost:' + port
    };
}