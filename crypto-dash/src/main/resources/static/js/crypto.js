function connect() {
    console.log("Connecting to websocket");
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/tickers', function(frame){
            // handleStateUpdate(JSON.parse(frame.body));
        });
    });
    stompClient.debug = null
}

function handleStateUpdate(state) {
    $.each(state, function(i, ticker) {
        var pairId = ticker.id;

        var currentClose = ticker.current.close;
        var currentElement = $("#Now" + "-" + pairId);
        currentElement.html(currentClose + "");
        $("#header-" + pairId).html(pairId + " - " + currentClose);

        var positiveCount = 0;

        $.each(ticker.snapshots, function(i, snapshot) {
            var name = snapshot.snapshotName;
            var close = snapshot.close;
            var change = calcChange(currentClose, close);
            var color = change < 0 ? "text-danger" : "text-success";

            if(change > 0) {
                positiveCount++;
            } else {
                positiveCount--;
            }

            var snapshotElement = $("#" + name + "-" + pairId);
            snapshotElement.html(close + " (" + change + "%)");
            snapshotElement.setAttribute("data-order", change);
            snapshotElement.removeClass();
            snapshotElement.addClass(color);
        });

        setAssetColor(pairId, positiveCount);
    })

}

function loadAssets() {
    $.get("/tickers", function(data) {
        var tb = drawTable();

        $.each(data, function(i, ticker) {
            renderTicker(ticker, tb)
        });

        tb.draw();

    });
}

function drawTable() {
    var tb = $("#cryptoTable");

    tb.DataTable({
        "paging":   false,
        "ordering": true,
        "info":     false,
        "searching": false,
        columns: [
            { data: 'Asset' },
            { data: 'Now' },
            { data: {
                _ : '1h.display',
                sort: '1h.change'
            }},
            { data: {
                _ : '4h.display',
                sort: '4h.change'
            }},
            { data: {
                _ : '12h.display',
                sort: '12h.change'
            }},
            { data: {
                _ : '24h.display',
                sort: '24h.change'
            }},
            { data: {
                _ : '7d.display',
                sort: '7d.change'
            }},
            {
                data: 'Actions',
                defaultContent: ""
            }
        ],
        'rowCallback': function(row, data, index){
            console.log("Row Callback: " + index);

            $(row).find('td:eq(1)').attr('class', 'text-info');
            setCellColor("1h", data, row, 2);
            setCellColor("4h", data, row, 3);
            setCellColor("12h", data, row, 4);
            setCellColor("24h", data, row, 5);
            setCellColor("7d", data, row, 6);

            var actionCell = $(row).find('td:eq(7)');
            var isTracked = isTrackedPair(data.Asset);
            if(isTracked) {
                actionCell.html("<button mode='untrack' class='btn btn-info btn-sm'>Untrack</button>")
            } else {
                actionCell.html("<button mode='track' class='btn btn-info btn-sm'>Track</button>")
            }
        }
    });

    $('#cryptoTable tbody').on( 'click', 'button', function () {
        var data = tb.DataTable().row( $(this).parents('tr') ).data();
        var pairId = data.Asset;
        var mode = $(this).attr("mode");

        var myPairs = Cookies.get('myPairs');

        if(mode === "track") {
            console.log("Track button clicked: " + pairId)

            var ds = "";
            if(myPairs !== undefined) {
                ds = myPairs
            }
            ds += pairId + ";";
            Cookies.set('myPairs', ds, { expires: 365 });

            $(this).text("Untrack");
            $(this).attr("mode", "untrack");
        } else {
            console.log("Untrack button clicked: " + pairId)

            Cookies.set("myPairs", myPairs.replace(pairId+";", ""), { expires: 365 });

            $(this).text("Track");
            $(this).attr("mode", "track");
        }
    } );

    return tb.dataTable().api();
}

function setCellColor(label, data, row, cell) {
    var td = $(row).find('td:eq(' + cell + ')');

    var colorClass = "text-primary";
    if(data[label].change < 0) {
        colorClass = "text-danger";
    } else if(data[label].change > 0) {
        colorClass = "text-success";
    }

    td.attr('class', colorClass);
}

function isTrackedPair(pair) {
    var myPairs = Cookies.get('myPairs');
    var pairs = myPairs.split(';');
    console.log("Tracked pairs: " + pairs)
    return pairs.find(function (element) {
        return element === pair
    });
}

function renderTicker(ticker, tb) {
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

        var currentClose = ticker.current.close;

        var d = {
            "Asset": ticker.pairName,
            "Now": ticker.current.close,
            "1h": buildSnapshot(ticker.snapshots, currentClose, "1h"),
            "4h": buildSnapshot(ticker.snapshots, currentClose, "4h"),
            "12h": buildSnapshot(ticker.snapshots, currentClose, "12h"),
            "24h": buildSnapshot(ticker.snapshots, currentClose, "24h"),
            "7d": buildSnapshot(ticker.snapshots, currentClose, "7d")

        };

        tb.row.add(d);


        $("#btn-" + ticker.pairName).one('click', function (e) {
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
                Cookies.set('myPairs', ds, { expires: 365 });

                $("#btn-" + pairId).html('Untrack')
            } else {
                console.log("Untrack button clicked: " + pairId)

                Cookies.set("myPairs", myPairs.replace(pairId+";", ""), { expires: 365 });

                $("#pair-" + pairId).remove()
            }
        })

    } else {
        console.log("Skipping rendering of asset: " + ticker.id)
    }
}

function buildSnapshot(snapshots, currentClose, label) {
    var snapshot = findSnapshot(snapshots, label).close;
    return {
        "display": snapshot + " (" + calcChange(currentClose, snapshot) + ")",
        "change": calcChange(currentClose, snapshot)
    }
}

function findSnapshot(snapshots, label) {

    for(var i = 0; i < snapshots.length; i++) {
        var s = snapshots[i];

        if(s.snapshotName === label) {
            return s;
        }
    }

    console.log("No match for label: " + label)
    return snapshots[0];
}

function setAssetColor(pairId, count) {
    var element = $("#pairColumn-" + pairId);

    element.removeClass();
    if(count < -2) {
        element.addClass("bg-danger")
    } else if(count > 2) {
        element.addClass("bg-success")
    }

}

function calcChange(currentClose, snapshotClose) {
    return (((currentClose / snapshotClose) * 100) - 100).toFixed(2);
}

function renderSnapshots(snapshots, id, current, tb) {
    var data = {
        name: "Now",
        pairName: id,
        close: current.close,
        change: "",
        textClass: "text-info"
    };

    var positiveCount = 0;

    var rendered = renderTemplate("snapshot", data);
    $("#snapshot-" + id).append(rendered);

    $.each(snapshots, function(i, snapshot) {
        var change = calcChange(current.close, snapshot.close);
        if(change > 0) {
            positiveCount++;
        } else {
            positiveCount--;
        }
        var color = change < 0 ? "text-danger" : "text-success";

        var data = {
            name: snapshot.snapshotName,
            pairName: id,
            close: snapshot.close,
            change: "(" + change +"%)",
            textClass: color
        };

        console.log("Rendering snapshot: " + snapshot.snapshotName + " for " + id)
        var rendered = renderTemplate("snapshot", data);
        $("#snapshot-" + id).append(rendered);

    });

    return positiveCount;
}

function renderTemplate(templateName, data) {
    var template = $('#' + templateName).html();
    Mustache.parse(template);
    return Mustache.render(template, data);
}

$(document).ready(function() {
    if(Cookies.get("myPairs") === undefined) {
        Cookies.set("myPairs", "", { expires: 365 })
    }

    loadAssets();

    $('#myTab').on('click', function (e) {
        e.preventDefault();

        // $("#content").empty();
        $("#myTab").addClass("active");
        $("#index").removeClass("active");

        console.log("Showing my crypto assets: " + Cookies.get("myPairs"));
        Cookies.set("mode", "myAssets", { expires: 365 });

        loadAssets()
    });

    $('#index').on('click', function (e) {
        e.preventDefault();

        // $("#content").empty();
        $("#myTab").removeClass("active");
        $("#index").addClass("active");

        Cookies.set("mode", "index", { expires: 365 });
        console.log("Showing all crypto assets");

        loadAssets()
    });

    connect();
});