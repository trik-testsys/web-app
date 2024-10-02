/**
 * Функция для закрытия попапа
 */
function closePopup() {
    document.getElementById('popup').style.display = 'none';
}

/**
 * Показываем попап, если он существует
 */
window.onload = function() {
    let popup = document.getElementById('popup');
    if (popup) {
        popup.style.display = 'block';
        // Закрыть попап через 5 секунд автоматически
        setTimeout(closePopup, 30_000);
    }
};