/**
 * Created by jiebaoqiang on 2018/7/24.
 */
$(function(){
    $("input[name='prodTypeCode']").val(prodType);
    // 点击提交验证
    $('button').on('click',function(){
        var that = $(this);
        var bgColor = $(this).css('background');
        var val = $('textarea').val();
        if(val){

            if(val.length > 300){
                $('.toast').text('内容不能超过300字').fadeIn().delay(2000).fadeOut()
                return;
            }
        }else{
            $('.toast').text('内容不能为空').fadeIn().delay(2000).fadeOut()
            return;
        }
        $(this).css({
            background: '#dcdcdc',
        }).attr('disabled', 'disabled');
        $.ajax({
                url: projectName+'/user/feedback',
                type: 'post',
                data:  $('form').serialize(),
                dataType: 'json',
                success: function (data) {
                    $('.toast').text(data.info).fadeIn().delay(2000).fadeOut();
                },
                complete: function() {
                that.css({
            background: bgColor,
        }).removeAttr('disabled');
    }
    })
    });

})
