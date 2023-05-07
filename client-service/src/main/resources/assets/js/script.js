var today = new Date();
var checkedDate = new Date(today).toISOString().split('T')[0];

jQuery(document).ready(function ($) {


    $('.member').on('click', function () {
        if (!$(this).hasClass('selected')) {
            $(this).addClass('selected');
            $('.wrap').addClass('member-selected');
            addCalendar($(this).find('.calendar'));
        }
    });
})

$('.deselect-member, .restart').on('click', function (e) {
    $('.member').removeClass('selected');
    $('.wrap').removeClass('member-selected date-selected slot-selected booking-complete');
    e.preventDefault();
    e.stopPropagation();
});

$('.deselect-date').on('click', function (e) {
    $('.wrap').removeClass('date-selected slot-selected');
    $('.calendar *').removeClass('selected');
    e.preventDefault();
    e.stopPropagation();
});

$('.deselect-slot').on('click', function (e) {
    $('.wrap').removeClass('slot-selected');
    $('.slots *').removeClass('selected');
    e.preventDefault();
    e.stopPropagation();
});

$('.form').on('submit', function (e) {
    $('.wrap').toggleClass('booking-complete');
    e.preventDefault();
    e.stopPropagation();
})


function invokeCalendarListener() {
    $('.calendar td:not(.disabled)').on('click', function (e) {
        var date = $(this).html();
        addSlots(date);
        var day = $(this).data('day');
        document.getElementById("date").value = checkedDate;
        $('.date').html(day + ',  ' + date);
        $(this).addClass('selected');
        setTimeout(function () {
            $('.wrap').addClass('date-selected');
        }, 10);
        e.preventDefault();
        e.stopPropagation();
    });
}


function invokeSlotsListener() {
    $('.slots li').on('click', function (e) {

        $(this).addClass('selected');
        document.getElementById("time").value = $(this).html();
        document.getElementById("visibleTime").innerText = $(this).html();

        $('.wrap').addClass('slot-selected');
        setTimeout(function () {
            $('.selected.member input[name="name"]').focus();
        }, 700);
        e.preventDefault();
        e.stopPropagation();
    });
}

function addSlots(date) {

    var number = Math.ceil(6 + 1);
    var time = 7;
    var timeDisplay = '';
    var slots = '';
    checkedDate = checkedDate.slice(0, 8) + (date.toString().length === 1 ? "0" + (date) : (date));

    var times = fetchTimes();

    times.forEach(function (elem) {
        time++;
        timeDisplay = elem.substring(0, 5);
        slots += '<li>' + timeDisplay + '</li>';
    })

    $('.selected .slots').html(slots);

    invokeSlotsListener();

}

function fetchDates() {

    var url = "http://45.159.249.5:8092/api/v1/clients/availableDates?agentId=" + therapistId + "&date=" + checkedDate;

    console.log(url)
    var xhr = new XMLHttpRequest();

    var result = [];

    xhr.open('GET', url, false);
    try {
        xhr.send();
        if (xhr.status !== 200) {
            alert(`Error ${xhr.status}: ${xhr.statusText}`);
        } else {
            let json_data = JSON.parse(xhr.response);
            for (let i in json_data) {
                var date = new Date(json_data[i]);
                if (date.getMonth() === today.getMonth()) {
                    result.push(date.getDate());
                }
            }
            return result;
        }
    } catch (err) { // instead of onerror
        alert("Request failed");
    }
}

function fetchTimes() {
    var url = "http://45.159.249.5:8092/api/v1/clients/availableTimes?agentId=" + therapistId + "&date=" + checkedDate;
    var xhr = new XMLHttpRequest();

    var result = [];
    xhr.open('GET', url, false);
    try {
        xhr.send();
        if (xhr.status != 200) {
            alert(`Error ${xhr.status}: ${xhr.statusText}`);
        } else {
            var json_data = JSON.parse(xhr.response);
            for (var i in json_data)
                result.push(json_data[i]);
            return result;
        }
    } catch (err) { // instead of onerror
        alert("Request failed");
    }
}

const currentDate = new Date();

function resetToFirstDayOfMonth() {
    today.setDate(1);
}

function nextMonth() {
    today.setMonth(today.getMonth() + 1);
    if (today.getMonth() !== currentDate.getMonth()) {
        resetToFirstDayOfMonth();
    }
    $('.member').removeClass('selected');
    $('.wrap').removeClass('member-selected date-selected slot-selected booking-complete');
}

function prevMonth() {
    today.setMonth(today.getMonth() - 1);
    if (today.getMonth() !== currentDate.getMonth()) {
        resetToFirstDayOfMonth();
    }
    $('.member').removeClass('selected');
    $('.wrap').removeClass('member-selected date-selected slot-selected booking-complete');
}

function addCalendar(container) {
    var date = today.getDate();
    var month = today.getMonth();
    var year = today.getFullYear();
    var first = new Date(today);

    first.setDate(0);
    var startDay = first.getDay();
    checkedDate = new Date(today).toISOString().split('T')[0];

    var dayLabels = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
    var monthLengths = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    var monthNames = ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'];
    var dayNames = ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'];

    var current = 1 - startDay;

    const prevMonth = (month + 11) % 12;
    const nextMonth = (month + 1) % 12;

    var calendar = '<label class="date"></label>' +
        '<label class="month">' +
        '<a class="prevMonth" onclick="prevMonth()"> ' + monthNames[prevMonth] + '</a>' +
        monthNames[month] +
        '<a class="nextMonth" onclick="nextMonth()">' + monthNames[nextMonth] + '</a></label>';

    calendar += '<table><tr>';
    dayLabels.forEach(function (label) {
        calendar += '<th>' + label + '</th>';
    })
    calendar += '</tr><tr>';

    let workDays = fetchDates();

    let dayClasses = '';
    while (current <= monthLengths[month]) {
        if (current > 0) {
            dayClasses = '';

            if (!workDays.includes(current)) {
                dayClasses += ' disabled';
            }

            if (current < date) {
                dayClasses += ' disabled';
            }

            if (current === date && (today.getMonth() === new Date().getMonth())) {
                dayClasses += ' today';
            }
            calendar += '<td class="' + dayClasses + '" data-day="' + dayNames[(current + startDay) % 7] + '">' + current + '</td>';
        } else {
            calendar += '<td></td>';
        }

        if ((current + startDay) % 7 == 0) {
            calendar += '</tr><tr>';
        }

        current++
    }

    calendar += '</tr></table>';
    container.html(calendar);

    invokeCalendarListener();

}
