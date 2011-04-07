$(document).ready(function(){

	// Collapse entries in each record.
        $('.collapseclass').collapser({
                target: 'next',
                targetOnly: 'div',
		changeText: 0,
                expandClass: 'expArrow',
                collapseClass: 'collArrow'
        });

	// OLD COLLAPSER
	// Entries in 'community-list' page should collapse.
	// pos = location.href.toLowerCase().indexOf( ('community-list').toLowerCase() );
	// if (pos != -1){

	//	$('.bold + ul').css('display','none');

        //	$('.bold').collapser({
        //        	target: 'siblings',
	//                targetOnly: 'ul',
        //	        changeText: 0,
        //        	expandClass: 'expArrow',
	//                collapseClass: 'collArrow'
        //	});

	// }

	// Hide the whole works if we're not on the community-list pages.
	pos = location.href.toLowerCase().indexOf( ('community-list').toLowerCase() );
        if (pos == -1){
		$('.ListMinus').hide();
		$('.ListPlus').hide();
	} else {
		$('.ds-artifact-item').css('list-style-type','none');
	}

	// Hide the Plus elements
	$('.ListMinus').hide();
	$('.ListMinus + span.bold ~ ul').hide();
	
	// Expansion
	$("p.ListPlus").click(function(){
		$(this).hide();
		$(this).next("p.ListMinus").show();
		//slideDown animation doesn't work in IE6:
		if(navigator.userAgent.match("MSIE 6")) 
		{
		    $(this).parent().find("p.ListPlus").hide();
		    $(this).parent().find("p.ListMinus").show();
		    $(this).parent().find("p.ListMinus + span.bold ~ ul").show();
		}
		else
		{
		    $(this).parent().children("ul").slideDown("fast");
		}
	});


	// Contraction
	$("p.ListMinus").click(function(){
		$(this).hide();
		$(this).prev("p.ListPlus").show();
		//slideUp animation doesn't work in IE6:
		if(navigator.userAgent.match("MSIE 6")) 
		{
		    $(this).parent().find("p.ListPlus").show();
		    $(this).parent().find("p.ListMinus").hide();
		    $(this).parent().find("p.ListMinus + span.bold ~ ul").hide();
		}
		else
		{
		    $(this).parent().children("ul").slideUp("fast");
		}
	});


});

