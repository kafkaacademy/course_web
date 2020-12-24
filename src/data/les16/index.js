
const topic = "myTopic";
document.getElementById('chatbox').innerText = topic;
const chat = document.getElementById('chatMessages');

const socket = new WebSocket("ws://localhost:8091/myChatApp?" + topic);
socket.onmessage = function (event) {
    chat.innerHTML += event.data + "<br />";
};

const input = document.getElementById('msg');
input.addEventListener("keyup", (event) => {
    if (event.keyCode === 13) {
        event.preventDefault();        
        send(input.value);
        input.value = '';
    }
});


function send(message) {
  
    if (socket.readyState == WebSocket.OPEN) {
        socket.send(message);
    } else {
        alert("The socket is not open.");
    }
    return false;
}