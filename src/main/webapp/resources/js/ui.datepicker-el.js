/* Greek (el) initialisation for the jQuery UI date picker plugin. */
/* Written by Alex Cicovic (http://www.alexcicovic.com) */
jQuery(function($){
        $.datepicker.regional['el'] = {
                closeText: '��������',
                prevText: '������������',
                nextText: '��������',
                currentText: '������ �����',
                monthNames: ['����������','�����������','�������','��������','�����','�������',
                '�������','���������','�����������','���������','���������','����������'],
                monthNamesShort: ['���','���','���','���','���','����',
                '����','���','���','���','���','���'],
                dayNames: ['�������','�������','�����','�������','������','���������','�������'],
                dayNamesShort: ['���','���','���','���','���','���','���'],
                dayNamesMin: ['��','��','��','��','��','��','��'],
                dateFormat: 'dd/mm/yy', firstDay: 1,
                isRTL: false};
        $.datepicker.setDefaults($.datepicker.regional['el']);
});