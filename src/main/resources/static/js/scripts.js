/**
 * Shows alert message
 * @param message text which will be shown
 */
function showAlert(message) {
    if (message) {
        alert(message); // Показываем alert с сообщением
    }
}


function copy(id) {
    var copyText = document.getElementById(id);
    var textArea = document.createElement("textarea");

    textArea.value = copyText.textContent;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand("Copy");
    textArea.remove();
}