
var url = "http://45.159.249.5:8092/api/v1/clients/services?agentId=" + therapistId;

var xhr = new XMLHttpRequest();

xhr.open('GET', url, false);
xhr.send();
if (xhr.status != 200) {
    alert(`Error ${xhr.status}: ${xhr.statusText}`);
} else {
    var data = JSON.parse(xhr.response);
    document.getElementById("serviceId").value = data.id;
    document.getElementById("name-field").innerText = data.name;
    document.getElementById("description-field").innerText = data.description;
    document.getElementById("price-field").innerText = data.price;
    document.getElementById("duration-field").innerText = data.duration;
    document.getElementById("agent-name-field").innerText = data.agentName;

    document.querySelector(".lds-ellipsis").remove();
    //document.getElementById("agent-phone-field").value = data.agentPhone;
}


var form = document.getElementById('myForm');
form.onsubmit = function(event){

    var xhr = new XMLHttpRequest();

    var startTime = document.querySelector("#date").value + " " + document.querySelector("#time").value;
    var serviceId = document.querySelector("#serviceId").value;

    var name = document.querySelector("#name").value;
    var phone = (document.querySelector("#phone").value).replace(/[^0-9]/g,"");

    let json = JSON.parse("{\n" +
        "  \"startTime\": \""+ startTime +"\",\n" +
        "  \"serviceId\": \"" + serviceId + "\",\n" +
        "  \"agentId\": \"" + therapistId + "\",\n" +
        "  \"client\": {\n" +
        "    \"name\": \"" + name + "\",\n" +
        "    \"phone\": \"" + phone + "\"\n" +
        "  }\n" +
        "}");
    xhr.open('POST','http://45.159.249.5:8092/api/v1/clients/appointment');
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.send(JSON.stringify(json));

    xhr.onreadystatechange = function() {
        if (xhr.readyState == XMLHttpRequest.DONE) {
            form.reset();
        }
    }
    return false;
}
