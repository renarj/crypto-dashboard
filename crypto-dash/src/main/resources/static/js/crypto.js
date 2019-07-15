function connect() {
    console.log("Connecting to websocket");
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/tickers', function(frame){
            handleStateUpdate(JSON.parse(frame.body));
        });
    });
    stompClient.debug = null
}

function handleStateUpdate(state) {
    $.each(state, function(i, ticker) {
        var pairId = ticker.id;

        var currentClose = ticker.current.close;
        var currentElement = $("#Now" + "-" + pairId);
        currentElement.html(currentClose + " (-%)");
        $("#header-" + pairId).html(pairId + " - " + currentClose);

        $.each(ticker.snapshots, function(i, snapshot) {
            var name = snapshot.snapshotName;
            var close = snapshot.close;
            var change = calcChange(currentClose, close);
            var color = change < 0 ? "text-danger" : "text-success";


            var snapshotElement = $("#" + name + "-" + pairId);
            snapshotElement.html(close + " (" + change + "%)");
            snapshotElement.removeClass();
            snapshotElement.addClass(color);
        })
    })

}

function loadDefaultAssets() {
    $.get("/tickers", function(data) {
        $.each(data, function(i, ticker) {
            renderTicker(ticker)
        })
    });
}

function loadFilteredAssets() {
    $.get("/tickers", function(data) {
        $.each(data, function(i, ticker) {
            renderTicker(ticker)
        })
    });
}

function renderTicker(ticker) {
    var myPairs = Cookies.get('myPairs');
    var pairs = myPairs.split(';');
    console.log("Tracked pairs: " + pairs)
    var isTrackedPair = pairs.find(function(element) {
        return element === ticker.pairName
    });
    console.log("Is tracked pair: " + isTrackedPair);

    console.log("Render mode: " + Cookies.get('mode'))

    var shouldRender = (isTrackedPair !== undefined && Cookies.get('mode') === "myAssets") || Cookies.get('mode') !== "myAssets";

    if(shouldRender) {
        console.log("Rendering ticker: " + ticker.id);
        var data = {
            pairName : ticker.pairName,
            close : ticker.current.close,
            mode : isTrackedPair !== undefined ? "Untrack" : "Track"
        };

        var rendered = renderTemplate("cryptoAsset", data)
        $("#content").append(rendered);

        console.log("Start rendering snapshtos");
        renderSnapshots(ticker.snapshots, ticker.id, ticker.current)
    } else {
        console.log("Skipping rendering of asset: " + ticker.id)
    }
}

function calcChange(currentClose, snapshotClose) {
    return (((currentClose / snapshotClose) * 100) - 100).toFixed(2);
}

function renderSnapshots(snapshots, id, current) {
    var data = {
        name: "Now",
        pairName: id,
        close: current.close,
        change: "-",
        textClass: "text-info"
    };

    var rendered = renderTemplate("snapshot", data);
    $("#snapshot-" + id).append(rendered);

    $.each(snapshots, function(i, snapshot) {
        var change = calcChange(current.close, snapshot.close);
        var color = change < 0 ? "text-danger" : "text-success";

        var data = {
            name: snapshot.snapshotName,
            pairName: id,
            close: snapshot.close,
            change: change,
            textClass: color
        };

        console.log("Rendering snapshot: " + snapshot.snapshotName + " for " + id)
        var rendered = renderTemplate("snapshot", data);
        $("#snapshot-" + id).append(rendered);

    });

    $("#btn-" + id).one('click', function (e) {
        e.preventDefault();

        var mode = this.getAttribute("mode");
        var myPairs = Cookies.get('myPairs');
        var pairId = this.getAttribute('pairId');

        if(mode === "Track") {
            console.log("Track button clicked: " + pairId)

            var ds = "";
            if(myPairs !== undefined) {
                ds = myPairs
            }
            ds += pairId + ";";
            Cookies.set('myPairs', ds)

            $("#btn-" + pairId).html('Untrack')
        } else {
            console.log("Untrack button clicked: " + pairId)

            Cookies.set("myPairs", myPairs.replace(pairId+";", ""));

            $("#pair-" + pairId).remove()
        }
    })
}

function renderTemplate(templateName, data) {
    var template = $('#' + templateName).html();
    Mustache.parse(template);
    return Mustache.render(template, data);
}

$(document).ready(function() {
    if(Cookies.get("myPairs") === undefined) {
        Cookies.set("myPairs", "")
    }

    if(Cookies.get('mode') === "myAssets") {
        loadFilteredAssets();
    } else {
        loadDefaultAssets();
    }


    $('#myTab').on('click', function (e) {
        e.preventDefault();

        $("#content").empty();
        $("#myTab").addClass("active");
        $("#index").removeClass("active");

        console.log("Showing my crypto assets: " + Cookies.get("myPairs"));
        Cookies.set("mode", "myAssets");

        loadFilteredAssets()
    });

    $('#index').on('click', function (e) {
        e.preventDefault();

        $("#content").empty();
        $("#myTab").removeClass("active");
        $("#index").addClass("active");

        Cookies.set("mode", "index");
        console.log("Showing all crypto assets");

        loadDefaultAssets()
    });

    connect()
});