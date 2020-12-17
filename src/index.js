import './css/style.css';

import prettier from 'prettier/standalone';
import parserHtml from "prettier/parser-html";
import parserBabel from "prettier/parser-babel";

const head = document.getElementsByTagName("head")[0];
const title = document.createElement("title");
title.innerText = "Apache Kafka Course";
head.appendChild(title);

const body = document.getElementsByTagName("body")[0];

const logo1 = document.createElement("div");
logo1.id = "logo1";

logo1.innerText="";
body.appendChild(logo1);

const logo2 = document.createElement("div");
logo2.id = "logo2";
logo2.innerText="";
body.appendChild(logo2);

const menu = document.createElement("nav");
menu.id = "nav_menu";
body.appendChild(menu);

const lessons = document.createElement("div");
lessons.id = "lessons";
body.appendChild(lessons);


var currentLesson = 0;
var lastLesson = 12;

const lessenEl = document.getElementById("lessons");

for (let i = 0; i <= lastLesson; i++) {
    let les = "les" + i;
    if (i < 10) les = "les0" + i;
    const teller = i;
    const lesEl = document.createElement("section");
    lesEl.classList = "section";
    lessenEl.appendChild(lesEl);
    lesEl.id = teller;
    lesEl.appendChild(createButtons(teller));
    const menuItem = document.createElement("a");
    menuItem.classList = "menu-item";
    menuItem.innerText = teller + " ";
    menuItem.href = "#";
    menuItem.addEventListener("click", () => toLesson(teller));
    menu.appendChild(menuItem);
    const url = './data/' + les + '.json';

    try {
        const request = async () => {
            const response = await fetch(url);
            const les = await response.json();
            const title = document.createElement("h1");
            title.classList = "title";

            title.innerHTML = "Lesson " + teller + " " + les.title;
            lesEl.appendChild(title);

            if (les.paragraphs != null) {
                for (let j = 0; j < les.paragraphs.length; j++) {
                    const p = les.paragraphs[j];
                    if (p == null) continue;
                    const parEl = document.createElement("div");
                    parEl.classList = ["paragraph"];
                    if (j%2==0)
                        parEl.classList.add("paragraph-even");                  
                    
                    else
                    parEl.classList.add("paragraph-uneven");                   
                  
                    lesEl.appendChild(parEl);

                    if (p.text != null) {
                        const p1 = document.createElement("p");
                        p1.classList = "paragraph";
                        parEl.appendChild(p1);
                        p1.innerHTML = p.text;
                    }
                    if (p.code != null) {
                        myCode(parEl,p.code);
                      
                    }
                    if (p.code2 != null) {                        
                        myCode(parEl,p.code2);                  
                    }
                    if (p.conclusion != null) {
                        const p1 = document.createElement("p");
                        p1.classList = "conclusion";
                        parEl.appendChild(p1);
                        p1.innerHTML = p.conclusion;
                    }
                }
            }

            lesEl.appendChild(createButtons(teller));
            setCurrentLesson();
        }
        request();
    } catch (error) {
        continue;
        console.log(error);
    }

}

function myCode(parEl, p_code) {
    if (p_code.code != null) {    //inline code                 
        const pre = document.createElement("pre");
        parEl.classList = "code";
        parEl.appendChild(pre);
        pre.innerText = p_code.code;
    }
    else {
        const language = p_code.language;
        const path = p_code.path;
        let index = path.lastIndexOf("/");
        if (index < 0) index = 0;
        else index++;
        const fname = path.substring(index);
        const div = document.createElement("div");
        div.classList = "download";
        parEl.appendChild(div);
        if (language === 'zip') {
            const a = document.createElement("a");
            a.href = p_code.path;
            a.innerText = "download " + fname;
            div.appendChild(a);
            a.target = "_blank";

        }
        else {
            const span = document.createElement("span");
            div.appendChild(span);

            const pre = document.createElement("pre");
            div.appendChild(pre);
            pre.style.display = "none";
            span.className = "caret";
            span.innerText = "show " + fname;

            getCode(language, path, pre);
            span.addEventListener("click", () => {
                if (pre.style.display === "none") {
                    pre.style.display = "block"
                    span.className = "caret-down"
                    span.innerText = "hide " + fname;
                }
                else {
                    pre.style.display = "none"
                    span.className = "caret";
                    span.innerText = "show " + fname;
                }
            });
        }
    }
}

function createButtons(teller) {
    const btnBox = document.createElement("div");
    btnBox.classList.add("buttonBox");
    btnBox.style.display = "flex";
    if (teller > 0) {
        const btn = document.createElement("section");
        btn.classList.add("button-previous");
        btn.innerText = "previous lesson";
        btn.addEventListener("click", (event) => previousLesson(event))
        btnBox.appendChild(btn);
    }
    if (teller < lastLesson) {
        const btn = document.createElement("section");
        btn.classList.add("button-next");
        btn.addEventListener("click", (event) => nextLesson(event));
        btn.innerText = "next lesson";
        btnBox.appendChild(btn);
    }
    return btnBox;

}
function nextLesson(event) {
    if (currentLesson < lastLesson) currentLesson++;
    setCurrentLesson();
}

function previousLesson(event) {
    if (currentLesson >= 0) currentLesson--;
    setCurrentLesson();
}

function toLesson(teller) {
    currentLesson = teller;
    setCurrentLesson();
}
function setCurrentLesson() {

    for (var i = 0; i <= lastLesson; i++) {
        const tmp = document.getElementById(i);
        if (tmp != null)
            tmp.style.display = "none";
    }

    const tmp = document.getElementById(currentLesson);
    if (tmp != null)
        tmp.style.display = "block";
}

function getCode(language, url, root) {
    try {
        const request = async () => {
            const response = await fetch(url);
            const les = await response.text();
            const testEl = document.getElementById(root);
            const pre = document.createElement("pre");
            pre.innerText = les;
            const btn = document.createElement("button");
            btn.innerText = "copy to clipboard";
            btn.addEventListener("click", () => copyToClipboard(les));
            pre.appendChild(btn);
            root.appendChild(pre);
        }
        request();
    } catch (error) {
        console.log(error);
    }

}

function copyToClipboard(text) {
    if (navigator.clipboard) { // default: modern asynchronous API
        return navigator.clipboard.writeText(text);
    } else if (window.clipboardData && window.clipboardData.setData) {     // for IE11
        window.clipboardData.setData('Text', text);
        return Promise.resolve();
    } else {
        // workaround: create dummy input
        console.log("workaround used");
        const input = document.createElement('input', { type: 'text' });
        input.value = text;
        document.body.append(input);
        input.focus();
        input.select();
        document.execCommand('copy');
        input.remove();
        return Promise.resolve();
    }
}



let x = prettier.format("<div><div>  abc</div><div>  abc</div><div>  abc</div><div>  abc</div><div>  abc</div><div>  abc</div><div>  abc</div><div>  abc</div><div>  abc</div><div>  abc</div><div>  abc</div></div>", {
    parser: "html",
    plugins: [parserHtml], useTabs: true
});

//console.log(x);

let javascript = prettier.format(`function setCurrentLesson() {

    for (var i = 0; i < lessons.length; i++) {
        document.getElementById(i).style.display = "none";
    }
    document.getElementById(currentLesson).style.display = "block";
}`, {
    parser: "babel",
    plugins: [parserBabel], useTabs: true, noBracketSpacing: true
});

