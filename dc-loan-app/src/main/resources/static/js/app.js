/**
 * Created by jiebaoqiang on 2018/8/1.
 * 与app交互js
 */
var prodType = getCookie('prodType');

$(function () {
    //返回App立即申请页面
    $(".goBackApp").on('click',function(){
        if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
            //苹果
            window.webkit.messageHandlers.goBackApp.postMessage(prodType);
        }else if (navigator.userAgent.match(/android/i)) {
            //安卓
            window.dc.goBackApp(prodType);
        }
    });
    //跳App上一个页面
    $(".previouStep").on('click',function(){

        var back = document.referrer;
        if(back != '' && back.indexOf('/form/paying')== -1 && back.indexOf('/form/jumpRepay') == -1){
            history.back();
        }else {
            if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
                //苹果
                window.webkit.messageHandlers.previouStep.postMessage(prodType);
            }else if (navigator.userAgent.match(/android/i)) {
                //安卓
                window.dc.previouStep(prodType);
            }
        }

    });

    /***
     * 签约完成跳原生待审核页面
     */
    $('.complete').on('click',function(){
        if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
            //苹果
            window.webkit.messageHandlers.openAudit.postMessage(prodType);
        }else if (navigator.userAgent.match(/android/i)) {
            //安卓
            window.dc.openAudit(prodType);
        }
    });

    /***
     * 跳京东
     */
    $('.jd').on('click',function(){
        if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
            //苹果
            window.webkit.messageHandlers.jdApp.postMessage(prodType);
        }else if (navigator.userAgent.match(/android/i)) {
            //安卓
            window.dc.jdApp(prodType);
        }
    });

    /***
     * 跳咸鱼
     */
    $('.xy').on('click',function(){
        if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
            //苹果
            window.webkit.messageHandlers.xyApp.postMessage(prodType);
        }else if (navigator.userAgent.match(/android/i)) {
            //安卓
            window.dc.xyApp(prodType);
        }
    });


})


//拨打客服电话
function callServicePhone(phone) {
    if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
        //苹果
        window.webkit.messageHandlers.callServicePhone.postMessage(phone);
    }else if (navigator.userAgent.match(/android/i)) {
        //安卓
        window.dc.callServicePhone(phone);
    }
}

function getCookie(sName)
{
    var aCookie = document.cookie.split("; ");
    for (var i=0; i < aCookie.length; i++)
    {
        var aCrumb = aCookie[i].split("=");
        if (sName == aCrumb[0])
            return unescape(aCrumb[1]);
    }
    return null;
}