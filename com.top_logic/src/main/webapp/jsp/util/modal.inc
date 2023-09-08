<!-- this are the divs for dialogs -->
<div id="mySplash"
	class="splashScreen"
>
	<div id="dialog"
		class="dialog"
	>
		<div
			onMouseUp="release()"
			onMousedown="selectElement(event,'dialog')"
			width="100%"
		>
			<table class="dialogTitleBar"
				border="0"
				width="100%"
			>
				<tr class="dialogTitleBar">
					<td class="dialogTitleBar">
						<span id="dialogTitle"
							style="white-space:nowrap;"
						>
							Dialog Title Bar
						</span>
					</td>
				</tr>
			</table>
		</div>
		<div style="text-align:center;">
			<span id="dialogLink">
			</span>
		</div>
	</div>
</div>