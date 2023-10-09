//region Scripts for change info popup
function showChangeInfoPopup() {
    var changeInfoPopup = document.getElementById("changeInfoPopup");
    changeInfoPopup.classList.add("show");
}

function closeChangeInfoPopup() {
    var changeInfoPopup = document.getElementById("changeInfoPopup");
    changeInfoPopup.classList.remove("show");

    var form = changeInfoPopup.querySelector("form");
    form.reset();
}

closeChangeInfo.addEventListener("click", function () {
    var changeInfoPopup = document.getElementById("changeInfoPopup");
    changeInfoPopup.classList.remove("show");
});

//endregion