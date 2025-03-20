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

function toggleVisibility(dependencyId, labelId, fieldId) {
    let dependency = document.getElementById(dependencyId);
    let label = document.getElementById(labelId);
    let field = document.getElementById(fieldId);


    if (dependency.checked) {
        label.setAttribute("hidden", ""); // Скрываем поле
        field.setAttribute("hidden", ""); // Скрываем поле

        field.removeAttribute("required"); // Убираем обязательность
    } else {
        label.removeAttribute("hidden"); // Показываем поле
        field.removeAttribute("hidden"); // Показываем поле

        field.setAttribute("required", ""); // Делаем обязательным
    }
}