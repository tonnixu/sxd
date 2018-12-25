/**
 * Created by jiebaoqiang on 2018/11/21.
 */
var num = $('.cardNum'),
    mainCardNum;
var manage = document.getElementById('manage');
num.map(function (i, v) {
    $(v).text('**** **** **** ' + $(v).text().slice(-4));
});

manage.addEventListener('click', function (e) {

    var res = $(e.target).hasClass('mainCard') || $(e.target).parents('li').hasClass('mainCard');

    if (!res) {
        $('.successModal').show();
        mainCardNum = $(e.target).data().id || $(e.target).parents('li').data().id
    }
})
$('.trueBtn').click(function () {

    $.ajax({
        url:projectName+'/bindCard/changeBank',
        type: "post",
        data:{
            perId:perId,
            bankNum: bankList[mainCardNum].bankNum
        },
        dataType: "json",
        success: function (data) {
            if (200 == data.code) {
                $('li').each(function (index,ele) {
                    var id = $(ele).data().id;
                    if (id == mainCardNum) {
                        $(ele).addClass('mainCard')
                    }else{
                        $(ele).removeClass('mainCard')
                    }
                });
            }else {

            }
        }

    });
    $('.successModal').hide()
})

$('.cancelBtn').click(function(){
    $('.successModal').hide();
})
/**
 * 跳转详情页
 */
$('.goBackDetail').click(function () {
    window.location.href = projectName +'/form/jumpDetails/'+phone+'/'+borrNum+'?prodType='+prodType;
})

/**
 *  添加银行卡
 */
$('.clickAddCard').click(function () {
 $('#addBank').submit();
})