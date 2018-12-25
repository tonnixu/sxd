var uploadElectronicCardInit = function(){
    checkPageEnabled("ym-va");


    $("#tg").load(function(){
        var tempText = $(this);
        //从后台传过来的数据，拿到就可以做相应的业务代码了
        var returnJson = tempText[0].contentDocument.body.textContent;
        var obj = eval('(' + returnJson + ')');
        alert(obj.msg)
    })

}



