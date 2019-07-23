!to"breakout.prg",cbm

;memory locations
background_color = $d021
border_color = $d020
control_register_1 = $d011
control_register_2 = $d016
vic_bank_select = $dd00
memory_control_register = $d018
screen_mem = $0400
color_mem = $d800
raster_line = 255
joystick_register = $DC00 ; port #2
GETIN = $ffe4

input_method = $02cb ; $00 = paddle, $01 = joystick

score_lsd = $02b2
score_msd = $02b3

ball_x = $02a7
ball_x_overflow = $02a8
ball_y = $02a9

bat_x = $02b7
bat_y = $02b8
bat_x_overflow = $02b9

lastchar = $02ca
game_on = $02c9 ; 0 = game not started, 1 = game on, 2 = game over
game_time_1 = $02cd
game_time_2 = $02ce
game_time_3 = $02cf


* = $0801
; startup basic code
  !by $0b,$08,$01,$00,$9e,$32,$30,$36,$31,$00,$00,$00 ;= SYS 2061

* = $080d ;=2061
  ; set colors
  lda #$00 ; black
  sta border_color
  lda #$01 ; white
  sta background_color
  lda #$01
  sta speed
  sta input_method 
  jsr draw_title
  jsr print_title_text
  ; breakpoint
  nop

forever:
- lda game_on
  cmp #2
  bne +
  ; breakpoint
  nop
+ 
  jsr GETIN
  beq -
  sta lastchar
  cmp #$85
  bne +
  jsr new_game ; F1
  lda #$01 
  sta game_on
  jmp forever
+ cmp #$86
  bne +
  jsr inc_speed ; F3
  lda game_on
  cmp #$00
  bne plus86
  jsr print_title_text
  jmp forever
plus86:
  cmp #$01
  bne forever
  jsr print_speed
  jmp forever
+ cmp #$87
  bne + 
  jsr dec_speed ; F5
  lda game_on
  cmp #$00
  bne plus87
  jsr print_title_text
  jmp forever
plus87:
  cmp #$01
  bne forever
  jsr print_speed
  jmp forever
+ cmp #$88
  bne +
  jsr toggle_input
  lda game_on
  cmp #$00
  bne forever
  jsr print_title_text
+ jmp forever

new_game:
  lda #$01 ; level = 1
  jsr reset_level
  jsr reset_score
  jsr draw_screen
  jsr initialize_sprites
  jsr initialize_movement
  jsr initialize_sound
  jsr print_speed
  jsr setup_raster_interrupts
  rts

draw_screen:
  ; clear screen with space character
  lda #$20
  ldx #$00
- sta screen_mem,x
  sta screen_mem + 256,x
  sta screen_mem + 512,x
  sta screen_mem + 768,x
  dex
  bne -
  jsr show_score_text
  jsr show_level_text
  jsr draw_tiles
  rts

setup_raster_interrupts:
  lda #%01111111
  sta $DC0D ; "Switch off" interrupts signals from CIA-1
  and $D011
  sta $D011 ; Clear most significant bit in VIC's raster register
  lda #raster_line
  sta $D012 ; Set the raster line number where interrupt should occur
  lda #<Irq
  sta $0314
  lda #>Irq
  sta $0315 ; Set the interrupt vector to point to interrupt service routine below
  lda #%00000001
  sta $D01A ; Enable raster interrupt signals from VIC
  rts
  
Irq:
; lda #2
; sta border_color
  lda game_on
  cmp #$01
  bne return_from_interrupt
  jsr reset_sound
  jsr increase_game_time
  lda input_method
  cmp #$00 
  bne + 
  jsr read_paddle
  jmp continue_irq
+ cmp #$01
  bne continue_irq
  jsr read_joystick
continue_irq:
  ; breakpoint for AI
  lda ball_y
  cmp #231
  bcc no_breakpoint
  lda pos_y_dir
  beq no_breakpoint
  nop
no_breakpoint:
  jsr move
  jsr check_bat_ball_collision
  jsr set_ball_and_bat
  ;lda sprite_sprite_collision
  ;beq +
  ;jsr bat_collision
;+ 
  jsr draw_tiles
  jsr check_next_level
  jsr calculate_score
  jsr update_score
  lda game_on
  cmp #$02 ; has the game just ended?
  bne return_from_interrupt
  jsr screen_off
  jsr play_game_over
  jsr show_game_over
  jsr screen_on
return_from_interrupt:
; lda #0
; sta border_color
  asl $D019 ; "Acknowledge" the interrupt by clearing the VIC's interrupt flag.
  jmp $EA31 ; Jump into KERNAL's standard interrupt service routine to handle keyboard scan, cursor display etc.
  
read_joystick:
  lda joystick_register
  and #$10 ; fire
  beq +
  jmp read_joystick_fire
+ lda joystick_register ; reverse logic; 0 = activated
  and #$01 ; up
  bne +
  jsr dec_bat_y
  jsr dec_bat_y
  jsr dec_bat_y
  jsr dec_bat_y
+ lda joystick_register
  and #$02 ; down
  bne +
  jsr inc_bat_y
  jsr inc_bat_y
  jsr inc_bat_y
  jsr inc_bat_y
+ lda joystick_register
  and #$04 ; left
  bne +
  jsr dec_bat_x
  jsr dec_bat_x
  jsr dec_bat_x
  jsr dec_bat_x
+ lda joystick_register
  and #$08 ; right
  bne +
  jsr inc_bat_x
  jsr inc_bat_x
  jsr inc_bat_x
  jsr inc_bat_x
+ rts

read_joystick_fire:
 lda joystick_register ; reverse logic; 0 = activated
  and #$01 ; up
  bne +
  jsr dec_bat_y
  jsr dec_bat_y
+ lda joystick_register
  and #$02 ; down
  bne +
  jsr inc_bat_y
  jsr inc_bat_y
+ lda joystick_register
  and #$04 ; left
  bne +
  jsr dec_bat_x
  jsr dec_bat_x
+ lda joystick_register
  and #$08 ; right
  bne +
  jsr inc_bat_x
  jsr inc_bat_x
+ rts

toggle_input:
  inc input_method
  lda input_method
  cmp #$02
  bne +
  lda #$00
  sta input_method
+ rts

increase_game_time:
  inc game_time_1
  bne increase_game_time_return
  inc game_time_2
  bne increase_game_time_return
  inc game_time_3
increase_game_time_return:
  rts

!source "paddle.asm"  
!source "tiles.asm"
!source "text.asm"
!source "movement.asm"
!source "sound.asm"
!source "sprites.asm"

sample = $2100
sample_end = $499D

* = $2100
!bin "gameover2.raw"

!source "title.asm"
