/**
 * Adds an event handler on all passed in divs to open datepicker on 'click'.
 * @assumption: all passed in divs are valid datepicker divs
 */
function triggerDatepickerOnClick(datepickerDivs) {
    $.each(datepickerDivs, (i, datepickerDiv) => {
        datepickerDiv.on('click', () => {
            if (!datepickerDiv.prop('disabled')) {
                datepickerDiv.datepicker('show');
            }
        });
    });
}

/**
 * @assumption: startDate has a valid value
 * @returns {Date} publishDate if it is valid and smaller than startDate, else startDate
 */
function getMaxDateForVisibleDate(startDate, publishDate) {
    let minDate = 0;

    if (publishDate === null || publishDate === undefined) {
        minDate = startDate;
    } else if (startDate > publishDate) {
        minDate = publishDate;
    } else {
        minDate = startDate;
    }

    return minDate;
}

/**
 * @assumption: visibleDate has a valid value
 * @returns {Date} visibleDate
 */
function getMinDateForPublishDate(visibleDate) {
    return visibleDate;
}

function prepareDatepickers() {
    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    $('#startdate').datepicker({
        dateFormat: 'dd/mm/yy',
        showOtherMonths: true,
        gotoCurrent: true,
        defaultDate: today,
        onSelect() {
            const newVisibleDate = getMaxDateForVisibleDate($('#startdate').datepicker('getDate'),
                    $('#publishdate').datepicker('getDate'));
            $('#visibledate').datepicker('option', 'maxDate', newVisibleDate);

            const newPublishDate = getMinDateForPublishDate($('#visibledate').datepicker('getDate'));
            $('#publishdate').datepicker('option', 'minDate', newPublishDate);
        },
    });

    $('#enddate').datepicker({
        dateFormat: 'dd/mm/yy',
        showOtherMonths: true,
        gotoCurrent: true,
        defaultDate: tomorrow,
    });

    $('#visibledate').datepicker({
        dateFormat: 'dd/mm/yy',
        showOtherMonths: true,
        gotoCurrent: true,
        defaultDate: yesterday,
        maxDate: today,
        onSelect() {
            const newPublishDate = getMinDateForPublishDate($('#visibledate').datepicker('getDate'));
            $('#publishdate').datepicker('option', 'minDate', newPublishDate);
        },
    });

    $('#publishdate').datepicker({
        dateFormat: 'dd/mm/yy',
        showOtherMonths: true,
        gotoCurrent: true,
        defaultDate: tomorrow,
        onSelect() {
            const newVisibleDate = getMaxDateForVisibleDate($('#startdate').datepicker('getDate'),
                    $('#publishdate').datepicker('getDate'));
            $('#visibledate').datepicker('option', 'maxDate', newVisibleDate);
        },
    });

    triggerDatepickerOnClick([$('#startdate'), $('#enddate'), $('#visibledate'), $('#publishdate')]);
}

export {
    getMaxDateForVisibleDate, // for test
    getMinDateForPublishDate, // for test
    prepareDatepickers,
    triggerDatepickerOnClick, // for test
};
