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
    var table = $("#cryptoTable").DataTable();

    $.each(state, function(i, ticker) {
        var pairId = ticker.id;

        var row = table.row('#' + pairId);

        if(row.length > 0) {
            var d = buildRowData(ticker);
            row.data(d);
        }
    });
    table.draw();
}

function loadAssets() {
    $.get("/tickers", function(data) {
        var tb = drawTable();
        tb.rows().remove();

        $.each(data, function(i, ticker) {
            renderTicker(ticker, tb)
        });

        tb.draw();

    });
}

function drawTable() {
    var tb = $("#cryptoTable");

    if (!$.fn.DataTable.isDataTable( '#cryptoTable' ) ) {
        console.log("Initiailising table");
        tb.DataTable({
            "paging":   false,
            "ordering": true,
            "info":     false,
            "searching": false,
            "rowId":'Asset',
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

                var count = 0;
                $(row).find('td:eq(1)').attr('class', 'text-info');
                count += setCellColor("1h", data, row, 2);
                count += setCellColor("4h", data, row, 3);
                count += setCellColor("12h", data, row, 4);
                count += setCellColor("24h", data, row, 5);
                count += setCellColor("7d", data, row, 6);

                setAssetBackground(row, count);

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
                console.log("Track button clicked: " + pairId);

                var ds = "";
                if(myPairs !== undefined) {
                    ds = myPairs
                }
                ds += pairId + ";";
                Cookies.set('myPairs', ds, { expires: 365 });

                $(this).text("Untrack");
                $(this).attr("mode", "untrack");
            } else {
                console.log("Untrack button clicked: " + pairId);

                Cookies.set("myPairs", myPairs.replace(pairId+";", ""), { expires: 365 });

                $(this).text("Track");
                $(this).attr("mode", "track");
            }
        } );
    } else {
        console.log("Table already initiailised");
    }

    return tb.dataTable().api();
}

function setAssetBackground(row, count) {
    var td = $(row).find('td:eq(0)');

    if(count < -2) {
        td.attr('class', 'bg-danger');
    } else if(count > 2) {
        td.attr('class', "bg-success");
    }

}

function setCellColor(label, data, row, cell) {
    var td = $(row).find('td:eq(' + cell + ')');

    var cnt = 0;
    var colorClass = "text-primary";
    if(data[label].change < 0) {
        colorClass = "text-danger";
        cnt = -1;
    } else if(data[label].change > 0) {
        colorClass = "text-success";
        cnt = 1;
    }

    td.attr('class', colorClass);
    return cnt;
}

function isTrackedPair(pair) {
    var myPairs = Cookies.get('myPairs');
    var pairs = myPairs.split(';');
    return pairs.find(function (element) {
        return element === pair
    });
}

function renderTicker(ticker, tb) {
    var myPairs = Cookies.get('myPairs');
    var pairs = myPairs.split(';');
    console.log("Tracked pairs: " + pairs);
    var isTrackedPair = pairs.find(function(element) {
        return element === ticker.pairName
    });
    console.log("Is tracked pair: " + isTrackedPair);

    console.log("Render mode: " + Cookies.get('mode'));

    var shouldRender = (isTrackedPair !== undefined && Cookies.get('mode') === "myAssets") || Cookies.get('mode') !== "myAssets";

    if(shouldRender) {
        console.log("Rendering ticker: " + ticker.id);
        tb.row.add(buildRowData(ticker));
    } else {
        console.log("Skipping rendering of asset: " + ticker.id)
    }
}

function buildRowData(ticker) {
    var currentClose = ticker.current.close;
    return {
        "Asset": ticker.pairName,
        "Now": ticker.current.close,
        "1h": buildSnapshot(ticker.snapshots, currentClose, "1h"),
        "4h": buildSnapshot(ticker.snapshots, currentClose, "4h"),
        "12h": buildSnapshot(ticker.snapshots, currentClose, "12h"),
        "24h": buildSnapshot(ticker.snapshots, currentClose, "24h"),
        "7d": buildSnapshot(ticker.snapshots, currentClose, "7d")
    };
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

    console.log("No match for label: " + label);
    return snapshots[0];
}

function calcChange(currentClose, snapshotClose) {
    return (((currentClose / snapshotClose) * 100) - 100).toFixed(2);
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