/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 28.66695856386578, "KoPercent": 71.33304143613422};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.23112950948002495, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.14293154331346566, 500, 1500, "EscInicio_GET_Raiz"], "isController": false}, {"data": [0.511166253101737, 500, 1500, "EscLogin_POST-0"], "isController": false}, {"data": [0.3926799007444169, 500, 1500, "EscLogin_POST-1"], "isController": false}, {"data": [0.7140270003292724, 500, 1500, "EscAdmin_GET_Proveedor-1"], "isController": false}, {"data": [0.7026176275679258, 500, 1500, "EscAPI_GET_Productos-1"], "isController": false}, {"data": [0.07730692461231384, 500, 1500, "EscLogin_GET"], "isController": false}, {"data": [0.4375, 500, 1500, "EscLogin_POST-2"], "isController": false}, {"data": [0.12299222797927462, 500, 1500, "EscAPI_GET_Productos"], "isController": false}, {"data": [0.12234910277324633, 500, 1500, "EscAdmin_GET_Proveedor"], "isController": false}, {"data": [0.023533054652422974, 500, 1500, "EscLogin_POST"], "isController": false}, {"data": [0.8987487652288443, 500, 1500, "EscAdmin_GET_Proveedor-0"], "isController": false}, {"data": [0.8918157720344599, 500, 1500, "EscAPI_GET_Productos-0"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 75369, 53763, 71.33304143613422, 246.6578434104208, 0, 11605, 266.0, 409.0, 1073.0, 4738.980000000003, 1249.2375522110985, 4588.7791816272875, 77.02205349151362], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["EscInicio_GET_Raiz", 17281, 14136, 81.8008217117065, 189.71211156761754, 0, 6773, 89.0, 393.0, 645.0, 1300.0, 287.15997274796854, 1907.7475667643612, 5.2566970776765976], "isController": false}, {"data": ["EscLogin_POST-0", 806, 0, 0.0, 1373.137717121589, 22, 6722, 795.0, 3566.700000000001, 4444.899999999998, 5730.439999999995, 13.388704318936876, 4.310355455772425, 4.279923042981728], "isController": false}, {"data": ["EscLogin_POST-1", 806, 371, 46.02977667493796, 379.28287841191076, 1, 5873, 143.0, 1022.3000000000001, 1272.9499999999998, 1982.6899999999982, 13.538254808096077, 52.871751516964814, 1.1740238147728228], "isController": false}, {"data": ["EscAdmin_GET_Proveedor-1", 3037, 517, 17.02337833388212, 312.10405004939116, 1, 6520, 104.0, 902.0, 1129.1, 1917.0999999999995, 50.572836874708585, 223.9272025442533, 6.597797533804036], "isController": false}, {"data": ["EscAPI_GET_Productos-1", 3018, 547, 18.124585818422798, 321.25447316103333, 1, 5889, 102.0, 918.0, 1141.0499999999997, 1968.2399999999998, 50.25226035266497, 221.59609178572023, 6.468968412299981], "isController": false}, {"data": ["EscLogin_GET", 6513, 5769, 88.57669276830954, 201.88684170121346, 0, 5812, 109.0, 378.0, 667.5999999999913, 1764.739999999997, 108.23251794735444, 303.0347958488434, 1.8793992050402153], "isController": false}, {"data": ["EscLogin_POST-2", 16, 6, 37.5, 482.5, 1, 1885, 210.5, 1703.7000000000003, 1885.0, 1885.0, 1.0198228057874943, 4.169920007648671, 0.10021452132067053], "isController": false}, {"data": ["EscAPI_GET_Productos", 15440, 12969, 83.99611398963731, 218.33205958549243, 0, 11605, 96.0, 400.0, 860.8999999999978, 1975.3600000000006, 256.5721692312805, 766.1814871090764, 14.646863393806708], "isController": false}, {"data": ["EscAdmin_GET_Proveedor", 15938, 13418, 84.18873133391894, 212.40249717655908, 1, 11584, 103.0, 399.0, 787.0, 1874.6100000000006, 264.8652241832021, 788.5994872681723, 14.926553489671619], "isController": false}, {"data": ["EscLogin_POST", 6459, 6030, 93.35810496980957, 332.1342313051566, 1, 11584, 118.0, 566.0, 1737.0, 3935.3999999999614, 107.2816662791084, 297.3126088710012, 5.46655052029698], "isController": false}, {"data": ["EscAdmin_GET_Proveedor-0", 3037, 0, 0.0, 273.78597299967095, 1, 6722, 92.0, 800.2000000000003, 1062.1999999999998, 1882.0, 50.506394372287176, 17.68066804144285, 8.348098188746237], "isController": false}, {"data": ["EscAPI_GET_Productos-0", 3018, 0, 0.0, 306.96255798542086, 1, 6724, 100.0, 809.0999999999999, 1088.0999999999995, 3370.43, 50.18958291758132, 17.570524533526243, 8.197168053199627], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 2553, 4.748618938675297, 3.3873343151693667], "isController": false}, {"data": ["Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 51210, 95.25138106132471, 67.94570712096485], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 75369, 53763, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 51210, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 2553, "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": ["EscInicio_GET_Raiz", 17281, 14136, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 13534, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 602, "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["EscLogin_POST-1", 806, 371, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 362, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 9, "", "", "", "", "", ""], "isController": false}, {"data": ["EscAdmin_GET_Proveedor-1", 3037, 517, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 515, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 2, "", "", "", "", "", ""], "isController": false}, {"data": ["EscAPI_GET_Productos-1", 3018, 547, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 546, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 1, "", "", "", "", "", ""], "isController": false}, {"data": ["EscLogin_GET", 6513, 5769, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 5443, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 326, "", "", "", "", "", ""], "isController": false}, {"data": ["EscLogin_POST-2", 16, 6, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 6, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["EscAPI_GET_Productos", 15440, 12969, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 12382, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 587, "", "", "", "", "", ""], "isController": false}, {"data": ["EscAdmin_GET_Proveedor", 15938, 13418, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 12728, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 690, "", "", "", "", "", ""], "isController": false}, {"data": ["EscLogin_POST", 6459, 6030, "Non HTTP response code: org.apache.http.conn.HttpHostConnectException/Non HTTP response message: Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", 5694, "Non HTTP response code: java.net.BindException/Non HTTP response message: Address already in use: connect", 336, "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
