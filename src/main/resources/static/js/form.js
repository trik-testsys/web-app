function enableForm(id) {
    const formElements = document.querySelectorAll('#' + id + ' input');
    formElements.forEach(input => input.className.includes('readonly') ? input.readOnly = true : input.readOnly = false);

    // Включаем кнопки "Сохранить" и "Отменить"
    document.getElementById(id + '-save').style.display = 'inline-block';
    document.getElementById(id + '-cancel').style.display = 'inline-block';

    // Скрываем кнопку "Редактировать"
    document.getElementById(id + '-edit').style.display = 'none';
}

function disableForm(id) {
    const formElements = document.querySelectorAll('#' + id + ' input');
    formElements.forEach(input => input.readOnly = true);

    // Отключаем кнопки "Сохранить" и "Отменить"
    document.getElementById(id + '-save').style.display = 'none';
    document.getElementById(id + '-cancel').style.display = 'none';

    // Показываем кнопку "Редактировать"
    document.getElementById(id + '-edit').style.display = 'inline-block';
}