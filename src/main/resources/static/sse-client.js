
function sendQuestion() {
    $("#yourQuestion").html("");
    $("#keywordSearchQuery").html("");
    $("#keywordSearchResult").html("");
    $("#similaritySearchResult").html("");
    $("#answer").html("");
    $("#workspace").html("");

    console.log("serverUrl: "+ serverUrl);
    console.log("username: "+ username);

    $("#yourQuestion").append(serverUrl+"?query="+username);
//    var query = $("#question").val();
//    console.log("query: "+ query);
    const sse = new EventSource(serverUrl+"?query="+username);

    sse.onmessage = function(event) {
        console.log(event.data);
        var result = JSON.parse(event.data);
        process(result);
        window.scrollTo(0, document.body.scrollHeight);
    }

    sse.onerror = function(error) {
        if(error.readyState == EventSource.CLOSED) {
            console.log("sse error - Closed");
        } else {
            console.log("sse error", error);
        }
    }
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

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $("#submit").click(() => sendQuestion());
    $("#workspace").hide();
});