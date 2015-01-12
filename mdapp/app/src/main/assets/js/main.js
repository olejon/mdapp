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

		var id = element.attr('rel');

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

                scrollToPosition(scroll_to);
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

function scrollToPosition(position)
{
    $('html, body').animate({ 'scrollTop': position }, 250, function()
    {
        Android.JSresetSpinner();
    });
}

function scrollToSection(id)
{
	var offset = $('a#'+id).offset();
	var position = offset.top + 12;

	scrollToPosition(position);
}