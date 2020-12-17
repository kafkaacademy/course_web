
const result = document.getElementById('result');
const errors = document.getElementById('errors');
const input = document.getElementById('bsn');
input.addEventListener("keyup", (event) => {
    if (event.keyCode === 13) {
        event.preventDefault();
        request(input.value);
        input.value = '';
    }
});

function getData(bsn) {
    const restUrl = 'http://localhost:8082/rest/' + bsn;
    fetch(restUrl).then(response => {
        if (response.status === 302) {
            return response.json()
        } else if (response.status === 404) {
            return Promise.reject('error 404')
        } else {
            return Promise.reject('some other error: ' + response.status)
        }
    }).then(data => {
        result.innerText += JSON.stringify(data) + "\n";

    }).catch(err => {
        errors.style.display = "block";
        errors.innerText = err + "(item not found) for bsn " + bsn;
    });
}


function request(bsn) {
    console.log("request for " + bsn);
    getData(bsn);
}