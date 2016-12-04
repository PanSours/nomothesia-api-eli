$(function () {
    $('html, body').animate({scrollTop: $('#${id}').position().top}, 'slow');
    return false;
});

$(document).ready(function () {
    //Check to see if the window is top if not then display button
    $(window).scroll(function () {
        if ($(this).scrollTop() > 100) {
            $('.scrollToTop').fadeIn();
        } else {
            $('.scrollToTop').fadeOut();
        }
    });

    //Click event to scroll to top
    $('.scrollToTop').click(function () {
        $('html, body').animate({scrollTop: 0}, 800);
        return false;
    });

});


function prepareList() {
    $('#messagescol').find('li:has(ul)')
        .click(function (event) {
            if (this == event.target) {
                $(this).toggleClass('expanded');
                $(this).children('ul').toggle('medium');
            }
            return false;
        })
        .addClass('collapsed')
        .children('ul').hide();
};

$(document).ready(function () {
    prepareList('&plusmn; ');
});

//CollapsibleLists.applyTo(document.getElementById('messages'));
//$(function (){
//    $('#messagescol').find('li:has(ul)').click(function(event) {
//        event.stopPropagation();
//    $(event.target).children('ul').slideToggle();
//    });
//});
