document.addEventListener('DOMContentLoaded', function () {
    function pad2(num) {
        return String(num).padStart(2, '0');
    }

    function formatLocal(date, includeSeconds) {
        var d = date;
        var day = pad2(d.getDate());
        var month = pad2(d.getMonth() + 1);
        var year = d.getFullYear();
        var hours = pad2(d.getHours());
        var minutes = pad2(d.getMinutes());
        var seconds = pad2(d.getSeconds());
        return includeSeconds ? (day + '.' + month + '.' + year + ' ' + hours + ':' + minutes + ':' + seconds)
                              : (day + '.' + month + '.' + year + ' ' + hours + ':' + minutes);
    }

    function toLocalDatetimeLocalValue(date) {
        var d = date;
        var year = d.getFullYear();
        var month = pad2(d.getMonth() + 1);
        var day = pad2(d.getDate());
        var hours = pad2(d.getHours());
        var minutes = pad2(d.getMinutes());
        return year + '-' + month + '-' + day + 'T' + hours + ':' + minutes;
    }

    var tz = null;
    try {
        tz = Intl.DateTimeFormat().resolvedOptions().timeZone || null;
    } catch (e) {
        tz = null;
    }

    document.querySelectorAll('input[type="hidden"][name="timezone"]').forEach(function (el) {
        if (tz) el.value = tz;
    });

    document.querySelectorAll('[data-utc]').forEach(function (el) {
        var iso = el.getAttribute('data-utc');
        if (!iso) return;
        var date = new Date(iso);
        if (isNaN(date.getTime())) return;

        if (el.tagName === 'INPUT' && el.getAttribute('type') === 'datetime-local') {
            el.value = toLocalDatetimeLocalValue(date);
        } else {
            var includeSeconds = el.hasAttribute('data-include-seconds');
            el.textContent = formatLocal(date, includeSeconds);
        }
    });
});


