// Window load

$(window).load(function()
{
	$('a.toggle-switch').each(function()
	{
		var element = $(this);

		var id = element.attr('rel');

		$('#'+id).hide();
	});

	$(document).on('click', 'a.toggle-switch', function()
	{
		var element = $(this);
		var next_element = element.next();

		var id = element.attr('rel');

        if(next_element.hasClass('media-wrapper')) next_element.children('div.video2').html('<video id="video" controls="controls" poster="http://www.felleskatalogen.no/m/medisin/film/'+id+'.jpg"><source src="http://www.felleskatalogen.no/m/medisin/film/'+id+'.mp4" type="video/mp4"></source></video>');

		var toggle_element = $('#'+id);

		if(toggle_element.is(':visible'))
		{
			toggle_element.hide();
		}
		else
		{
			toggle_element.show();

			if(id == 'begrep')
			{
				var scroll_top = $(window).scrollTop();
				var scroll_to = scroll_top + 256;

				scrollToPosition(scroll_to, true);
			}
		}
	});

	$(document).on('click', 'span.ordbok', function()
	{
		var element = $(this);

		var classes = element.attr('class');

		var id = classes.match(/ordbok\s+(\d+)-\d+/);

		if(id != null)
		{
			var definition = $('p#begrep-'+id[1]).html();

			var title = definition.match(/<span class="synonym">([^<]+)<\/span>/);
			var message = definition.match(/<span class="definition">([^<]+)<\/span>/);

			if(title != null && message != null) Android.JSshowDialog(title[1], message[1].replace(/&nbsp;/g, ' '));
		}
	});
});

// Functions

function scrollToPosition(position, animate)
{
    if(animate)
    {
        $('html, body').animate({ 'scrollTop': position }, 250, function()
        {
            Android.JSresetSpinner();
        });
    }
    else
    {
        window.scrollTo(0, position);
    }
}

function scrollToSection(id, animate)
{
	var offset = $('a#'+id).offset();
	var position = offset.top + 12;

	scrollToPosition(position, animate);
}

function scrollToPositionAfterFindInText()
{
    var scroll_top = $(window).scrollTop();
    var scroll_to = scroll_top + 128;

    window.scrollTo(0, scroll_to);
}

function pauseVideos()
{
	var elements = document.getElementsByTagName('video');

	for(var i = 0; i < elements.length; i++)
	{
		elements[i].pause();
	}
}