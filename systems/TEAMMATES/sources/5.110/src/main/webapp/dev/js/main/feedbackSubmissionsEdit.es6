/* global tinymce:false */

import {
    showModalAlert,
} from '../common/bootboxWrapper.es6';

import {
    StatusType,
} from '../common/const.es6';

import {
    isNumber,
} from '../common/helper.es6';

import {
    richTextEditorBuilder,
} from '../common/richTextEditor.es6';

import {
    sanitizeForJs,
} from '../common/sanitizer.es6';

import {
    scrollToElement,
} from '../common/scrollTo.es6';

import {
    setStatusMessage,
} from '../common/statusMessage.es6';

import {
    bindLinksInUnregisteredPage,
} from '../common/student.es6';

import {
    addLoadingIndicator,
    disallowNonNumericEntries,
} from '../common/ui.es6';

const FEEDBACK_RESPONSE_RECIPIENT = 'responserecipient';
const FEEDBACK_RESPONSE_TEXT = 'responsetext';
const FEEDBACK_MISSING_RECIPIENT = 'You did not specify a recipient for your response in question(s)';
const WARNING_STATUS_MESSAGE = '.alert-warning.statusMessage';
const END_TIME = '#end-time';
const MS_IN_FIFTEEN_MINUTES = 900000;

// text displayed to user
const SESSION_NOT_OPEN = 'Feedback Session Not Open';
const SESSION_CLOSING_HEADER = 'Feedback Session Will Be Closing Soon';
const SESSION_CLOSING_MESSAGE = 'Warning: you have less than 15 minutes before the submission deadline expires!';

function isPreview() {
    return $(document).find('.navbar').text().indexOf('Preview') !== -1;
}

function isModeration() {
    return $('#moderationHintButton').length !== 0;
}

function updateOtherOptionAttributes(otherOption, indexSuffix) {
    if (otherOption.is(':checked')) {
        $(`#msqOtherOptionText${indexSuffix}`).prop('disabled', false); // enable textbox
        $(`#msqIsOtherOptionAnswer${indexSuffix}`).val('1');
    } else {
        $(`#msqOtherOptionText${indexSuffix}`).prop('disabled', true); // disable textbox
        $(`#msqIsOtherOptionAnswer${indexSuffix}`).val('0');
    }
}

function bindModerationHintButton() {
    if (!isModeration()) {
        return;
    }

    const expandText = '[More]';
    const closeText = '[Less]';
    const $moderationHintButton = $('#moderationHintButton');
    const $moderationHint = $('#moderationHint');

    $moderationHintButton.text(expandText);

    $moderationHintButton.click((event) => {
        event.preventDefault();

        if ($moderationHint.hasClass('hidden')) {
            $moderationHintButton.text(closeText);
            $moderationHint.removeClass('hidden');
        } else {
            $moderationHintButton.text(expandText);
            $moderationHint.addClass('hidden');
        }
    });
}

function getQuestionTypeNumbers(qnType) {
    const questions = $('input[name^="questiontype-"]').filter(function () {
        return $(this).val() === qnType;
    });

    const questionNums = [];

    for (let i = 0; i < questions.length; i += 1) {
        questionNums[i] = questions[i].name.substring('questiontype-'.length, questions[i].name.length);
    }

    return questionNums;
}

// Saves the value in the other option textbox for MCQ questions
function updateMcqOtherOptionField() {
    const mcqQuestionNums = getQuestionTypeNumbers('MCQ');

    for (let i = 0; i < mcqQuestionNums.length; i += 1) {
        const qnNum = mcqQuestionNums[i];
        const numResponses = $(`[name="questionresponsetotal-${qnNum}"]`).val();

        for (let j = 0; j < numResponses; j += 1) {
            $(`[data-text="otherOptionText"][name="responsetext-${qnNum}-${j}"]`)
                 .val($(`#otherOptionText-${qnNum}-${j}`).val());
        }
    }
}

// Saves the value in the other option textbox for MSQ questions
function updateMsqOtherOptionField() {
    const msqQuestionNums = getQuestionTypeNumbers('MSQ');

    for (let i = 0; i < msqQuestionNums.length; i += 1) {
        const qnNum = msqQuestionNums[i];
        const numResponses = $(`[name="questionresponsetotal-${qnNum}"]`).val();

        for (let j = 0; j < numResponses; j += 1) {
            $(`[data-text="msqOtherOptionText"][name="responsetext-${qnNum}-${j}"]`)
                 .val($(`#msqOtherOptionText-${qnNum}-${j}`).val());
        }
    }
}

// Looks for the question to be moderated (if it exists)
function focusModeratedQuestion() {
    if ($('#moderated-question').length > 0) {
        scrollToElement($('#moderated-question')[0], { duration: 1000 });
    }
}

function prepareMCQQuestions() {
    const mcqQuestionNums = getQuestionTypeNumbers('MCQ');

    const radioButtons = {};
    const radioStates = {};

    $.each(mcqQuestionNums, (i) => {
        const qnNum = mcqQuestionNums[i];
        const numResponses = $(`[name="questionresponsetotal-${qnNum}"]`).val();

        for (let j = 0; j < numResponses; j += 1) {
            const id = `responsetext-${qnNum}-${j}`;
            radioButtons[id] = $(`[name=${id}]`);
            radioStates[id] = {};

            // initialize radio buttons' states
            $.each(radioButtons[id], (index, radio) => {
                radioStates[id][radio.value] = $(radio).is(':checked');
            });

            radioButtons[id].click(function (event) {
                const val = $(this).val();
                const name = $(this).attr('name');
                const indexSuffix = name.substring(name.indexOf('-'));

                // toggle the radio button checked state
                $(this).attr('checked', radioStates[name][val] = !radioStates[name][val]);

                // If the radio button corresponding to 'Other' is clicked
                if ($(this).data('text') === 'otherOptionText') {
                    if ($(this).is(':checked')) {
                        $(`#otherOptionText${indexSuffix}`).prop('disabled', false); // enable textbox
                        $(`#mcqIsOtherOptionAnswer${indexSuffix}`).val('1');
                    } else {
                        $(`#otherOptionText${indexSuffix}`).prop('disabled', true); // disable textbox
                        $(`#mcqIsOtherOptionAnswer${indexSuffix}`).val('0');
                    }
                } else if ($(`#mcqIsOtherOptionAnswer${indexSuffix}`).length > 0) {
                    // If other option is enabled for the question
                    $(`#otherOptionText${indexSuffix}`).prop('disabled', true); // disable textbox
                    $(`#mcqIsOtherOptionAnswer${indexSuffix}`).val('0');
                }

                $.each(radioButtons[name], (index, radio) => {
                    if (radio.value !== val) {
                        radioStates[name][radio.value] = false;
                    }
                });

                event.stopImmediatePropagation();
            });
        }
    });
}

function prepareContribQuestions() {
    const contribQuestionNums = getQuestionTypeNumbers('CONTRIB');

    $.each(contribQuestionNums, (i) => {
        const qnNum = contribQuestionNums[i];

        // Get options for the specified question number of contribution question type
        const options = $(`[name^="responsetext-${qnNum}-"]`);

        $.each(options, (k) => {
            const $dropdown = $(`[name="responsetext-${qnNum}-${k}"]`);

            // Set initial color
            $dropdown.addClass($dropdown[0].options[$dropdown[0].selectedIndex].className);

            $dropdown.on('change', function () {
                $(this).removeClass('color_neutral');
                $(this).removeClass('color-positive');
                $(this).removeClass('color-negative');
                $(this).addClass(this.options[this.selectedIndex].className);
            });
        });
    });
}

function prepareMSQQuestions() {
    // Get index of MSQ questions
    const msqQuestionNums = getQuestionTypeNumbers('MSQ');

    $.each(msqQuestionNums, (i) => {
        const qnNum = msqQuestionNums[i];

        let noneOfTheAboveOption = $(`input[name^="responsetext-${qnNum}-"][value=""]:not([data-text])`);
        const otherOption = $(`input[name^="responsetext-${qnNum}-"][data-text="msqOtherOptionText"]`);

        // If 'other' is enabled for the question
        if (otherOption.length > 0) {
            // checkbox corresponding to 'other' is clicked
            otherOption.click(function () {
                const name = $(this).attr('name');
                const indexSuffix = name.substring(name.indexOf('-'));
                updateOtherOptionAttributes($(this), indexSuffix);
            });
        }

        // reset other options when "none of the above" is clicked
        noneOfTheAboveOption.click(function () {
            const $options = $(this).closest('table')
                                  .find('input[name^="responsetext-"][value!=""], '
                                        + 'input[name^="responsetext-"][data-text]'); // includes 'other'
            const name = $(this).attr('name');
            const indexSuffix = name.substring(name.indexOf('-'));

            $options.each(function () {
                $(this).prop('checked', false);

                // 'other' option is clicked
                if ($(this).attr('data-text') !== undefined) {
                    updateOtherOptionAttributes($(this), indexSuffix);
                }
            });
        });

        // reset "none of the above" if any option is clicked
        const $options = $(`input[name^="responsetext-${qnNum}-"][value!=""], `
                        + `input[name^="responsetext-${qnNum}-"][data-text]`); // includes 'other'

        $options.click(function () {
            noneOfTheAboveOption = $(this).closest('table').find(
                                           'input[name^="responsetext-"][value=""]:not([data-text])');
            const name = $(this).attr('name');
            const indexSuffix = name.substring(name.indexOf('-'));

            noneOfTheAboveOption.prop('checked', false);

            // 'other' option is clicked
            if ($(this).attr('data-text') !== undefined) {
                updateOtherOptionAttributes($(this), indexSuffix);
            }
        });
    });
}

/**
 *  Updates the colour of a rubric cell if it is checked.
 */
function updateRubricCellSelectedColor(radioInput) {
    const cell = $(radioInput).parent();
    const tableRow = cell.parent();

    if ($(radioInput).prop('checked')) {
        cell.addClass('cell-selected');
        tableRow.addClass('row-answered');
    } else if (cell.hasClass('cell-selected')) {
        cell.removeClass('cell-selected');
    }
}

/**
 * Syncs the mobile ui for rubrics on changes to the desktop ui
 */
function syncRubricsMobileUi(changedInput) {
    const $changedInput = $(changedInput);
    const mobileInputId = `#mobile-${changedInput.id}`;
    const mobileInputName = `[name^="mobile-${changedInput.name}"]`;
    if ($changedInput.is(':checked')) {
        $(mobileInputId).click();
    } else {
        $(mobileInputName).prop('checked', false);
        $(mobileInputId).trigger('change', [true]);
    }
}

/**
 * Syncs the desktop ui for rubrics on changes to the mobile ui
 */
function syncRubricsDesktopUi(changedInput) {
    const $changedInput = $(changedInput);
    const desktopInputId = `#${changedInput.id.replace('mobile-', '')}`;
    const desktopInputName = `[name^="${changedInput.name.replace('mobile-', '')}"]`;
    if ($changedInput.is(':checked')) {
        $(desktopInputId).click();
    } else {
        $(desktopInputName).prop('checked', false);
        $(desktopInputId).trigger('change', [true]);
    }
}

/**
 * Prepares desktop view for rubric questions by setting cells to highlight on hover, highlight when checked
 * and bind the cells click to click radio buttons
 */
function prepareDesktopRubricQuestions() {
    const $rubricRadioInputs = $('[name^="rubricChoice-"]');

    $.each($rubricRadioInputs, (i) => {
        const $parentCell = $($rubricRadioInputs[i]).parent();

        $parentCell.hover(function () {
            $(this).addClass('cell-hover');
        }, function () {
            $(this).removeClass('cell-hover');
        });

        $parentCell.click(function (event) {
            const $radioInput = $(this).find('[name^="rubricChoice-"]');

            if ($radioInput.prop('disabled')) {
                return;
            }

            if (event.target === this) {
                $radioInput.prop('checked', !$radioInput.prop('checked'));
                $radioInput.trigger('change');
            }
        });

        // Bind refresh highlights on check
        $($rubricRadioInputs[i]).on('change', function (event, isSync) {
            // Update all radio inputs in the same row.
            const $rowRadioInputs = $(this).closest('tr').find('[name^="rubricChoice-"]');
            const tableRow = $(this).closest('tr');

            if (tableRow.hasClass('row-answered')) {
                tableRow.removeClass('row-answered');
            }

            for (let j = 0; j < $rowRadioInputs.length; j += 1) {
                updateRubricCellSelectedColor($rowRadioInputs[j]);
            }

            if (isSync === undefined) {
                // Sync mobile UI
                syncRubricsMobileUi(this);
            }
        });

        // First time update of checked cells
        for (let j = 0; j < $rubricRadioInputs.length; j += 1) {
            updateRubricCellSelectedColor($rubricRadioInputs[j]);
        }
    });
}

/**
 * Prepares mobile view for rubric questions by setting panels
 * to change color on clicking the radio buttons and uncheck logic
 */
function prepareMobileRubricQuestions() {
    const $rubricRadioInputs = $('[name^="mobile-rubricChoice-"]');

    // setup initial panel colors
    const $filledInPanels = $rubricRadioInputs.filter(':checked').closest('.panel');
    $filledInPanels.removeClass('panel-default').addClass('panel-success');

    // setup panel highlighting when changing an option
    $rubricRadioInputs.on('change', function (event, isSync) {
        const $self = $(this);
        const $parentPanel = $self.closest('.panel');
        if ($self.is(':checked')) {
            $parentPanel.removeClass('panel-default').addClass('panel-success');
        } else {
            $parentPanel.addClass('panel-default').removeClass('panel-success');
        }
        if (isSync === undefined) {
            syncRubricsDesktopUi(this);
        }
    });

    // setup unchecking when clicking on selected radio button
    // reference: http://stackoverflow.com/a/6246260
    $rubricRadioInputs.closest('label').mousedown(function () {
        const $self = $(this);
        const $radioInput = $self.find('[name^="mobile-rubricChoice-"]');
        if ($radioInput.is(':checked') && !$radioInput.prop('disabled')) {
            const uncheck = function () {
                setTimeout(() => {
                    $radioInput.prop('checked', false);
                    $radioInput.trigger('change');
                }, 0);
            };
            const unbind = function () {
                $self.unbind('mouseup', up); // eslint-disable-line no-use-before-define
            };
            const up = function () {
                uncheck();
                unbind();
            };
            $self.bind('mouseup', up);
            $self.one('mouseout', unbind);
        }
    });
}

function prepareRubricQuestions() {
    prepareDesktopRubricQuestions();
    prepareMobileRubricQuestions();
}

function formatRubricQuestions() {
    const rubricQuestionNums = getQuestionTypeNumbers('RUBRIC');
    for (let i = 0; i < rubricQuestionNums.length; i += 1) {
        const qnNum = rubricQuestionNums[i];
        let numResponses = $(`[name="questionresponsetotal-${qnNum}"]`).val();
        numResponses = parseInt(numResponses, 10);

        for (let j = 0; j < numResponses; j += 1) {
            const responsetext = [];

            const $responses = $(`[name^="rubricChoice-${qnNum}-${j}-"]:checked`);

            for (let k = 0; k < $responses.length; k += 1) {
                responsetext.push($($responses[k]).val());
            }

            $(`[name="responsetext-${qnNum}-${j}"]`).val(responsetext);
        }
    }
}

function prepareConstSumQuestions() {
    const constSumQuestionNums = getQuestionTypeNumbers('CONSTSUM');

    for (let i = 0; i < constSumQuestionNums.length; i += 1) {
        const qnNum = constSumQuestionNums[i];

        if (!$('#response_submit_button').is(':disabled')
            || isPreview()) {
            if ($(`#constSumToRecipients-${qnNum}`).val() === 'true') {
                let numResponses = $(`[name="questionresponsetotal-${qnNum}"]`).val();
                numResponses = parseInt(numResponses, 10);

                $(`#constSumInfo-${qnNum}-${numResponses - 1}`).show();
            }
        } else {
            $(`[id^="constSumInfo-${qnNum}-"]`).hide();
        }
    }
}

function updateConstSumMessageQn(qnNum) {
    let numOptions = 0;
    let points = parseInt($(`#constSumPoints-${qnNum}`).val(), 10);
    const numRecipients = parseInt($(`[name="questionresponsetotal-${qnNum}"]`).val(), 10);
    const distributeToRecipients = $(`#constSumToRecipients-${qnNum}`).val() === 'true';
    const pointsPerOption = $(`#constSumPointsPerOption-${qnNum}`).val() === 'true';
    const forceUnevenDistribution = $(`#constSumUnevenDistribution-${qnNum}`).val() === 'true';

    if (distributeToRecipients) {
        numOptions = numRecipients;
    } else {
        numOptions = parseInt($(`#constSumNumOption-${qnNum}`).val(), 10);
    }

    if (pointsPerOption) {
        points *= numOptions;
    }

    let sum = 0;
    let remainingPoints = points;
    let allUnique = true;
    let allNotNumbers = true;
    let answerSet = {};

    function checkAndDisplayMessage(messageElement) {
        let message = '';

        if (allNotNumbers) {
            message = `Please distribute ${points} points among the above ${
                       distributeToRecipients ? 'recipients.' : 'options.'}`;
            messageElement.addClass('text-color-blue');
            messageElement.removeClass('text-color-red');
            messageElement.removeClass('text-color-green');
        } else if (remainingPoints === 0) {
            if (!forceUnevenDistribution || allUnique) {
                message = 'All points distributed!';
                messageElement.addClass('text-color-green');
                messageElement.removeClass('text-color-red');
                messageElement.removeClass('text-color-blue');
            }
        } else if (remainingPoints > 0) {
            message = `${remainingPoints} points left to distribute.`;
            messageElement.addClass('text-color-red');
            messageElement.removeClass('text-color-green');
            messageElement.removeClass('text-color-blue');
        } else {
            message = `Over allocated ${-remainingPoints} points.`;
            messageElement.addClass('text-color-red');
            messageElement.removeClass('text-color-green');
            messageElement.removeClass('text-color-blue');
        }

        if (!allNotNumbers && forceUnevenDistribution && !allUnique) {
            message += ' The same amount of points should not be given multiple times.';
            messageElement.addClass('text-color-red');
            messageElement.removeClass('text-color-green');
        }

        messageElement.text(message);
    }

    function updateSumBasedOn(ptsAllocatedParam) {
        let pointsAllocated = ptsAllocatedParam;
        if (isNumber(pointsAllocated)) {
            allNotNumbers = false;
        } else {
            pointsAllocated = 0;
        }

        sum += pointsAllocated;

        if (pointsAllocated in answerSet) {
            allUnique = false;
        }

        answerSet[pointsAllocated] = true;
    }

    if (distributeToRecipients) {
        const $constSumMessageElement = $(`#constSumMessage-${qnNum}-${numOptions - 1}`);

        for (let i = 0; i < numOptions; i += 1) {
            const pointsAllocated = parseInt($(`#${FEEDBACK_RESPONSE_TEXT}-${qnNum}-${i}-0`).val(), 10);

            updateSumBasedOn(pointsAllocated);
        }

        remainingPoints = points - sum;

        checkAndDisplayMessage($constSumMessageElement);
    } else {
        for (let j = 0; j < numRecipients; j += 1) {
            sum = 0;
            allNotNumbers = true;
            answerSet = {};
            allUnique = true;
            remainingPoints = points;

            const $constSumMsgElement = $(`#constSumMessage-${qnNum}-${j}`);

            for (let k = 0; k < numOptions; k += 1) {
                const ptsAllocated = parseInt($(`#${FEEDBACK_RESPONSE_TEXT}-${qnNum}-${j}-${k}`).val(), 10);

                updateSumBasedOn(ptsAllocated);
            }

            remainingPoints = points - sum;

            checkAndDisplayMessage($constSumMsgElement);
        }
    }
}

function updateConstSumMessages() {
    const constSumQuestionNums = getQuestionTypeNumbers('CONSTSUM');

    for (let i = 0; i < constSumQuestionNums.length; i += 1) {
        const qnNum = constSumQuestionNums[i];
        updateConstSumMessageQn(qnNum);
    }
}

function validateConstSumQuestions() {
    updateConstSumMessages();

    // When any of the const sum questions has an error.
    if ($('p[id^="constSumMessage-"].text-color-red').length > 0) {
        const constSumQuestionNums = getQuestionTypeNumbers('CONSTSUM');
        let statusMessage = 'Please fix the error(s) for distribution question(s)';
        let errorCount = 0;

        for (let i = 0; i < constSumQuestionNums.length; i += 1) {
            const qnNum = constSumQuestionNums[i];

            // indicate the question number where the errors are located at
            if ($(`p[id^="constSumMessage-${qnNum}-"].text-color-red`).length > 0) {
                statusMessage += errorCount === 0 ? '' : ',';
                statusMessage += ' ';
                statusMessage += qnNum;
                errorCount += 1;
            }
        }

        statusMessage += '. ';
        statusMessage += 'To skip a distribution question, leave the boxes blank.';

        setStatusMessage(statusMessage, StatusType.DANGER);
        return false;
    }

    return true;
}

/**
 * Removes already selected options for recipients from other select dropdowns within the same question.
 * Binds further changes to show/hide options such that duplicates cannot be selected.
 */
function formatRecipientLists() {
    $('select.participantSelect').each(function () {
        if (!$(this).hasClass('.newResponse')) {
            // Remove options from existing responses
            const questionNumber = $(this).attr('name').split('-')[1];
            let selectedOption = $(this).find('option:selected').val();

            if (selectedOption !== '') {
                selectedOption = sanitizeForJs(selectedOption);
                $(`select[name|=${FEEDBACK_RESPONSE_RECIPIENT}-${questionNumber}]`)
                    .not(this)
                    // leave this in double quotes and single within, will fail otherwise
                    .find(`option[value='${selectedOption}']`)
                        .hide();
            }
        }

        // Save initial data.
        $(this).data('previouslySelected', $(this).val());
    }).change(function () {
        const questionNumber = $(this).attr('name').split('-')[1];
        const lastSelectedOption = $(this).data('previouslySelected');
        let curSelectedOption = $(this).find('option:selected').val();

        if (lastSelectedOption !== '') {
            $(`select[name|=${FEEDBACK_RESPONSE_RECIPIENT}-${questionNumber}]`)
                .not(this)
                // leave this in double quotes and single within, will fail otherwise
                .find(`option[value='${lastSelectedOption}']`)
                    .show();
        }

        if (curSelectedOption !== '') {
            curSelectedOption = sanitizeForJs(curSelectedOption);
            $(`select[name|=${FEEDBACK_RESPONSE_RECIPIENT}-${questionNumber}]`)
                .not(this)
                // leave this in double quotes and single within, will fail otherwise
                .find(`option[value='${curSelectedOption}']`)
                    .hide();
        }

        // Save new data
        $(this).data('previouslySelected', $(this).val());
    });

    // Auto-select first valid option.
    $('select.participantSelect.newResponse').each(function () {
        let firstUnhidden = '';

        // select the first valid recipient if the dropdown is hidden from the user,
        // otherwise, leave it as ""
        if (this.style.display === 'none') {
            $($(this).children().get().reverse()).each(function () {
                if (this.style.display !== 'none' && $(this).val() !== '') {
                    firstUnhidden = this;
                }
            });
        }

        $(this).val($(firstUnhidden).val()).change();
    });
}

function reenableFieldsForSubmission() {
    $(':disabled').prop('disabled', false);
}

function validateNumScaleAnswer(qnIdx, responseIdx) {
    const $answerBox = $(`[name=responsetext-${qnIdx}-${responseIdx}]`);
    const min = parseInt($answerBox.attr('min'), 10);
    const max = parseInt($answerBox.attr('max'), 10);
    const answer = parseInt($answerBox.val(), 10);

    if (answer < min) {
        $answerBox.val($answerBox.attr('min'));
    } else if (answer > max) {
        $answerBox.val($answerBox.attr('max'));
    }
}

function isAnswerBlank(question, response) {
    const $answer = $(`[name=responsetext-${question}-${response}]`);

    if ($answer.attr('type') === 'radio' || $answer.attr('type') === 'checkbox') {
        // for question types that involve checking boxes such as MSQ, MCQ
        return !$answer.is(':checked');
    }
    return $answer.val().trim() === '';
}

// Checks that there are no responses written to an unspecified recipient
function validateAllAnswersHaveRecipient() {
    const blankRecipients = $('select[name^="responserecipient-"]').filter(function () {
        return $(this).val() === '';
    });

    let isAllAnswersToMissingRecipientEmpty = true;
    let statusMessage = FEEDBACK_MISSING_RECIPIENT;
    let errorCount = 0;

    // for every response without a recipient, check that the response is empty
    for (let i = 0; i < blankRecipients.length; i += 1) {
        const recipient = blankRecipients[i];

        const question = $(recipient).attr('name').split('-')[1];
        const response = $(recipient).attr('name').split('-')[2];

        if (!isAnswerBlank(question, response)) {
            statusMessage += errorCount === 0 ? '' : ',';
            statusMessage += ' ';
            statusMessage += question;
            errorCount += 1;

            isAllAnswersToMissingRecipientEmpty = false;
        }
    }

    if (!isAllAnswersToMissingRecipientEmpty) {
        setStatusMessage(`${statusMessage}.`, StatusType.DANGER);
    }

    return isAllAnswersToMissingRecipientEmpty;
}

function updateRankMessageQn(qnNum) {
    const isDistributingToRecipients = $(`#rankToRecipients-${qnNum}`).val() === 'true';
    const areDuplicateRanksAllowed = $(`#rankAreDuplicatesAllowed-${qnNum}`).val() === 'true';
    const numRecipients = parseInt($(`[name="questionresponsetotal-${qnNum}"]`).val(), 10);

    const numOptions = isDistributingToRecipients ? numRecipients
                                                : parseInt($(`#rankNumOptions-${qnNum}`).val(), 10);

    let areAllAnswersUnique;
    let allocatedRanks;
    let isAllOptionsRanked;

    function resetState() {
        allocatedRanks = {};
        areAllAnswersUnique = true;
        isAllOptionsRanked = true;
    }

    function updateRankMessagesInUpdatingRankMessageQn($messageElement) {
        $messageElement.removeClass('text-color-red text-color-green text-color-blue');

        let message = '';

        if (!areDuplicateRanksAllowed && !areAllAnswersUnique) {
            message += ' The same rank should not be given multiple times. ';
            $messageElement.addClass('text-color-red');
        } else if (!isAllOptionsRanked) {
            message = `Please rank the above ${isDistributingToRecipients ? 'recipients. '
                                                                             : 'options. '}`;
            $messageElement.addClass('text-color-blue');
        }

        $messageElement.text(message);
        if (message === '') {
            $messageElement.parent().find('hr').hide();
        } else {
            $messageElement.parent().find('hr').show();
        }
    }

    function updateAllocatedRanks(rankAllocated) {
        if (!isNumber(rankAllocated)) {
            isAllOptionsRanked = false;
            return;
        }
        if (rankAllocated in allocatedRanks) {
            areAllAnswersUnique = false;
        }

        allocatedRanks[rankAllocated] = true;
    }

    function updateDropdownOptions(questionNumber, recipientIndex) {
        const dropdownSelect = $(`select[id^="responsetext-${questionNumber}-${recipientIndex}-"]`);

        dropdownSelect.find('option').each(function () {
            if ($(this).val() in allocatedRanks) {
                $(this).addClass('color_neutral');
            } else {
                $(this).removeClass('color_neutral');
            }
        });
    }

    if (isDistributingToRecipients) {
        // for Rank Recipients question
        resetState();

        const $rankMessageElement = $(`#rankMessage-${qnNum}-${numOptions - 1}`);

        for (let i = 0; i < numOptions; i += 1) {
            const rankAllocated = parseInt($(`#${FEEDBACK_RESPONSE_TEXT}-${qnNum}-${i}-0`).val(), 10);
            updateAllocatedRanks(rankAllocated);
        }
        for (let j = 0; j < numOptions; j += 1) {
            updateDropdownOptions(qnNum, j);
        }

        updateRankMessagesInUpdatingRankMessageQn($rankMessageElement);
    } else {
        // for Rank options question
        for (let i1 = 0; i1 < numRecipients; i1 += 1) {
            resetState();

            const $rankMsgElement = $(`#rankMessage-${qnNum}-${i1}`);

            for (let j1 = 0; j1 < numOptions; j1 += 1) {
                const rankAlloc = parseInt($(`#${FEEDBACK_RESPONSE_TEXT}-${qnNum}-${i1}-${j1}`).val(), 10);
                updateAllocatedRanks(rankAlloc);
            }

            updateDropdownOptions(qnNum, i1);
            updateRankMessagesInUpdatingRankMessageQn($rankMsgElement);
        }
    }
}

function updateRankMessages() {
    const rankQuestionNums = getQuestionTypeNumbers('RANK_OPTIONS').concat(getQuestionTypeNumbers('RANK_RECIPIENTS'));

    for (let i = 0; i < rankQuestionNums.length; i += 1) {
        const qnNum = rankQuestionNums[i];
        updateRankMessageQn(qnNum);
    }
}

function prepareRankQuestions() {
    const rankQuestionNums = getQuestionTypeNumbers('RANK_OPTIONS').concat(getQuestionTypeNumbers('RANK_RECIPIENTS'));

    for (let i = 0; i < rankQuestionNums.length; i += 1) {
        const qnNum = rankQuestionNums[i];

        const isRankingRecipients = $(`#rankToRecipients-${qnNum}`).val() === 'true';

        if (!$('#response_submit_button').is(':disabled')
            || isPreview()) {
            if (isRankingRecipients) {
                let numResponses = $(`[name="questionresponsetotal-${qnNum}"]`).val();
                numResponses = parseInt(numResponses, 10);

                $(`#rankInfo-${qnNum}-${numResponses - 1}`).show();
            }
        } else {
            $(`[id^="rankInfo-${qnNum}-"]`).hide();
        }
    }
    updateRankMessages();
}

function validateRankQuestions() {
    updateRankMessages();

    // if any of the rank questions has an error.
    if ($('p[id^="rankMessage-"].text-color-red').length > 0) {
        const rankQuestionNums = getQuestionTypeNumbers('RANK_OPTIONS').concat(getQuestionTypeNumbers('RANK_RECIPIENTS'));
        let statusMessage = 'Please fix the error(s) for rank question(s)';
        let errorCount = 0;

        for (let i = 0; i < rankQuestionNums.length; i += 1) {
            const qnNum = rankQuestionNums[i];

            // indicate the question number where the errors are located at
            if ($(`p[id^="rankMessage-${qnNum}-"].text-color-red`).length > 0) {
                statusMessage += errorCount === 0 ? '' : ',';
                statusMessage += ' ';
                statusMessage += qnNum;
                errorCount += 1;
            }
        }

        statusMessage += '. ';
        statusMessage += 'To skip a rank question, leave all the boxes blank.';

        setStatusMessage(statusMessage, StatusType.DANGER);
        return false;
    }

    return true;
}

function hasWarningMessage() {
    return $(WARNING_STATUS_MESSAGE).length;
}

function isSessionClosingSoon() {
    const endTimeData = $(END_TIME).data('end-time');
    if (!endTimeData) {
        return false;
    }
    const endDate = new Date(endTimeData);
    const currentDate = new Date();
    const remainingTime = endDate - currentDate;
    return remainingTime <= MS_IN_FIFTEEN_MINUTES && remainingTime > 0;
}

function getWarningMessage() {
    return $(WARNING_STATUS_MESSAGE).html().trim();
}

function showModalWarningIfSessionClosed() {
    if (hasWarningMessage()) {
        showModalAlert(SESSION_NOT_OPEN, getWarningMessage(), null, StatusType.WARNING);
    }
}

function showModalWarningIfSessionClosingSoon() {
    if (isSessionClosingSoon()) {
        showModalAlert(SESSION_CLOSING_HEADER, SESSION_CLOSING_MESSAGE, null, StatusType.WARNING);
    }
}

/**
 * Updates the length of the textArea
 * @param textAreaId - Id of text area for which char are to be counted
 * @param wordsCountId - Id of Label to display length of text area
 */
function updateTextQuestionWordsCount(textAreaId, wordsCountId, recommendedLength) {
    const editor = tinymce.get(textAreaId);
    if (!editor) {
        return;
    }

    const response = $(editor.getContent()).text();
    const $wordsCountElement = $(`#${wordsCountId}`);

    const wordsCount = response.split(/\s/g).filter(item => item.match(/\w/)).length;

    $wordsCountElement.text(wordsCount);

    const upperLimit = recommendedLength + recommendedLength * 0.1;
    const lowerLimit = recommendedLength - recommendedLength * 0.1;

    if (wordsCount > lowerLimit && wordsCount < upperLimit) {
        $wordsCountElement.css('color', 'green');
    } else {
        $wordsCountElement.css('color', 'gray');
    }
}

$(document).ready(() => {
    const textFields = $('div[id^="responsetext-"]');

    if (typeof richTextEditorBuilder !== 'undefined') {
        $.each(textFields, (i, textField) => {
            const id = $(textField).attr('id');

            /* eslint-disable camelcase */ // The property names are determined by external library (tinymce)
            richTextEditorBuilder.initEditor(`#${id}`, {
                inline: true,
                setup(ed) {
                    ed.on('keyup', function () {
                        updateTextQuestionWordsCount(id, $(textField).data('lengthTextId'), $(this).data('recommendedText'));
                    });
                    ed.on('keydown', function () {
                        updateTextQuestionWordsCount(id, $(textField).data('lengthTextId'), $(this).data('recommendedText'));
                    });
                    ed.on('init', function () {
                        updateTextQuestionWordsCount(id, $(textField).data('lengthTextId'), $(this).data('recommendedText'));
                    });
                    ed.on('change', function () {
                        updateTextQuestionWordsCount(id, $(textField).data('lengthTextId'), $(this).data('recommendedText'));
                    });
                },
            });
            /* eslint-enable camelcase */
        });
    }

    $('form[name="form_submit_response"]').submit((e) => {
        formatRubricQuestions();

        const validationStatus = validateConstSumQuestions()
                                 && validateRankQuestions()
                                 && validateAllAnswersHaveRecipient();

        updateMcqOtherOptionField();
        updateMsqOtherOptionField();

        if (!validationStatus) {
            e.preventDefault();
            e.stopPropagation();
        } else {
            reenableFieldsForSubmission(); // only enabled inputs will appear in the post data

            // disable button to prevent user from clicking submission button again
            const $submissionButton = $('#response_submit_button');
            addLoadingIndicator($submissionButton, '');
        }
    });

    formatRecipientLists();

    // Replace hidden dropdowns with text
    $('select.participantSelect:hidden').each(function () {
        $(this).after(`<span>${$(this).find('option:selected').html()}</span>`);
    });

    $("input[type='radio']").change(function () {
        const idOfOtherOptionText = `otherOptionText${$(this).attr('name').substr($(this).attr('name').search('-'))}`;
        const idOfOtherOptionFlag = `otherOptionFlag${$(this).attr('name').substr($(this).attr('name').search('-'))}`;

        if ($(this).data('text') === 'otherOptionText') {
            // Other option is selected by the student
            $(`#${idOfOtherOptionText}`).prop('disabled', false);
            $(`#${idOfOtherOptionFlag}`).val('1');
        } else {
            // Any option except the other option is selected
            $(`#${idOfOtherOptionText}`).prop('disabled', true);
            $(`#${idOfOtherOptionFlag}`).val('0');
        }
    });

    $("input[id^='otherOptionText']").keyup(function () {
        const idOfOtherOptionRadioButton = $(this).attr('id').replace('Text', '');
        $(`#${idOfOtherOptionRadioButton}`).val($(this).val());
    });

    disallowNonNumericEntries($('input[type=number]'), true, true);

    $('input.pointsBox').off('keydown');

    disallowNonNumericEntries($('input.pointsBox'), false, false);

    prepareContribQuestions();

    prepareMSQQuestions();

    prepareConstSumQuestions();

    updateConstSumMessages();

    prepareRubricQuestions();

    prepareMCQQuestions();

    prepareRankQuestions();

    focusModeratedQuestion();

    bindModerationHintButton();

    showModalWarningIfSessionClosed();

    showModalWarningIfSessionClosingSoon();

    bindLinksInUnregisteredPage('[data-unreg].navLinks');
});

window.validateNumScaleAnswer = validateNumScaleAnswer;
window.updateConstSumMessageQn = updateConstSumMessageQn;
window.updateRankMessageQn = updateRankMessageQn;
