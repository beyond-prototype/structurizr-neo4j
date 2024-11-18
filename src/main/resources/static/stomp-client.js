//let username = "web";
const stompClient = new StompJs.Client({
    brokerURL: stompBrokerURL
    ,connectHeaders: {
        login: username,
        passcode:'1234'
    }
    ,debug: function (str) {
        console.log(str);
    }
    ,reconnectDelay:5000
    ,heartbeatIncoming:4000
    ,heartbeatOutgoing:4000
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/user/queue/saac', (output)=> {
        console.log('Received output: '+output.body);
        //console.log(JsON.parse(output.body));
        var result = JSON.parse(output.body);
        process(result);
        window.scrollTo(0, document.body.scrollHeight);
    });

    stompClient.subscribe('/topic/greetings', (output) => {
        showGreeting(JSON.parse(output.body).content);
        console.log('greetings-Received output: '+output.body);
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    $("#answer").html("");
}

function sendName() {
    stompClient.publish({
        destination: "/app/hello",
        body: JSON.stringify({'name': $("#name").val()})
    });
}

function showGreeting(message) {
    $("#answer").append(message);
}

$(function () {

    stompClient.activate();
    //stompClient.deactivate();

    $("form").on('submit', (e) => e.preventDefault());

    $("#submit").click(() => sendQuestion());

    $("#workspace").hide();
});

function sendQuestion() {
    $("#yourQuestion").html("");
    $("#keywordSearchQuery").html("");
    $("#keywordSearchResult").html("");
    $("#similaritySearchResult").html("");
    $("#answer").html("");
    $("#workspace").html("");

    stompClient.publish({
        destination:"/app/saac",
        body: JSON.stringify({'query': $("#question").val()})
    });
}

function process(result) {
    if(result.question !== undefined) {
        $("#yourQuestion").append(result.question);
    }
    else if(result.cypherQuery !== undefined) {
        $("#keywordSearchQuery").append(result.cypherQuery);
    }
    else if(result.keywordSearchResult !== undefined) {
        $("#keywordSearchResult").append(result.keywordSearchResult);
    }
    else if(result.similaritySearchResult !== undefined) {
        $("#similaritySearchResult").append(result.similaritySearchResult);
    }
    else if(result.answer !== undefined) {
        $("#answer").append(result.answer);
    }
    else if(result.dsl !== undefined) {
        var workspace = $("#workspace");
        workspace.show();
        workspace.append(result.dsl);
        if(workspace.length){
            workspace.scrollTop(workspace[0].scrollHeight - workspace.height());
        }
    }
    else if(result.status !== undefined) {

    }
}