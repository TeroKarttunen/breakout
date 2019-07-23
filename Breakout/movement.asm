pos_y_dir = $02ab
pos_x_dir = $02ac

speed = $02bf ; $00 = 2, $01 = 3, $02 = 4, $03 = 6
movement_counter = $02c0
movement_dir = $02c1 ; $00-$03
movement_temp_a = $02c2
movement_temp_x = $02c3

initialize_movement:
  lda #$01
  sta movement_counter
  lda #$00
  sta movement_dir
  sta pos_y_dir
  lda #$01
  sta pos_x_dir
  rts

move:
  ldx speed
  bne +
  jmp move_speed_2
+ dex
  bne +
  jmp move_speed_3
+ dex 
  bne +
  jmp move_speed_4
+ dex
  bne +
  jmp move_speed_6
+ rts

move_speed_3:
  ldx movement_counter
  lda movement_dir
  cmp #$01
  bne +
  txa
  clc
  adc #$04
  tax
+ lda movement_dir
  cmp #$02
  bne +
  txa
  clc
  adc #$08
  tax
+ lda movement_dir
  cmp #$03
  bne +
  txa
  clc
  adc #$0c
  tax
+ lda speed3, x
  jsr process_move
  inc movement_counter
  lda movement_counter
  cmp #$04
  bne +
  lda #$00
  sta movement_counter
+ rts

move_speed_6:
  ldx movement_counter
  lda movement_dir
  cmp #$01
  bne +
  txa
  clc
  adc #$02
  tax
+ lda movement_dir
  cmp #$02
  bne +
  txa
  clc
  adc #$04
  tax
+ lda movement_dir
  cmp #$03
  bne +
  txa
  clc
  adc #$06
  tax
+ lda speed6, x
  jsr process_move
  inc movement_counter
  lda movement_counter
  cmp #$02
  bne +
  lda #$00
  sta movement_counter
+ rts


move_speed_4:
  ldx movement_counter
  lda movement_dir
  cmp #$01
  bne +
  txa
  clc
  adc #$03
  tax
+ lda movement_dir
  cmp #$02
  bne +
  txa
  clc
  adc #$06
  tax
+ lda movement_dir
  cmp #$03
  bne +
  txa
  clc
  adc #$09
  tax
+ lda speed4, x
  jsr process_move
  inc movement_counter
  lda movement_counter
  cmp #$03
  bne +
  lda #$00
  sta movement_counter
+ rts


  
move_speed_2:
  ldx movement_counter
  lda movement_dir
  cmp #$01
  bne +
  txa
  clc
  adc #$06
  tax
+ lda movement_dir
  cmp #$02
  bne +
  txa
  clc
  adc #$0c
  tax
+ lda movement_dir
  cmp #$03
  bne +
  txa
  clc
  adc #$12
  tax
+ lda speed2, x
  jsr process_move
  inc movement_counter
  lda movement_counter
  cmp #$06
  bne +
  lda #$00
  sta movement_counter
+ rts
  
process_move:
  ldx #$00
- asl
  inx
  bcc -
process_loop:
  cpx #$08
  beq process_return
  asl
  inx
  bcc +
  sta movement_temp_a
  stx movement_temp_x
  jsr move_y
  lda movement_temp_a
  ldx movement_temp_x
  jmp process_loop
+ sta movement_temp_a
  stx movement_temp_x
  jsr move_x
  lda movement_temp_a
  ldx movement_temp_x
  jmp process_loop
process_return:
  rts
  
move_x:
  lda #$00
  sta tiles_hit_counter
  lda pos_x_dir
  bne move_x_positive
  jsr dec_ball_x
  bcc +
  lda #$01
  sta pos_x_dir
+ jsr handle_collision
  lda tiles_hit_counter
  beq +
  sta add_to_score
  jsr increase_score
  jsr tiles_sound
  lda #$01 ; collision with tile
  sta pos_x_dir
  jsr inc_ball_x ; bounce
+ rts
move_x_positive:
  jsr inc_ball_x
  bcc +
  lda #$00
  sta pos_x_dir
+ jsr handle_collision
  lda tiles_hit_counter
  beq +
  sta add_to_score
  jsr increase_score
  jsr tiles_sound
  lda #$00 ; collision with tile
  sta pos_x_dir
  jsr dec_ball_x ; bounce
+ rts

move_y:
  lda #$00
  sta tiles_hit_counter
  lda pos_y_dir
  bne move_y_positive
  jsr dec_ball_y
  bcc +
  lda #$01
  sta pos_y_dir
+ jsr handle_collision
  lda tiles_hit_counter
  beq +
  sta add_to_score
  jsr increase_score
  jsr tiles_sound
  lda #$01 ; collision with tile
  sta pos_y_dir
  jsr inc_ball_y ; bounce
+ jmp move_return
move_y_positive:
  jsr inc_ball_y
  bcc +
  lda #$02
  sta game_on
  sta pos_y_dir
+ jsr handle_collision
  lda tiles_hit_counter
  beq +
  sta add_to_score
  jsr increase_score
  jsr tiles_sound
  lda #$00 ; collision with tile
  sta pos_y_dir
  jsr dec_ball_y ; bounce
+ jmp move_return
move_return:
  rts
  
inc_speed:
  lda speed
  cmp #$03
  beq +
  inc speed
  lda #$00
  sta movement_counter
+ rts

dec_speed:
  lda speed
  cmp #$00
  beq +
  dec speed
  lda #$00
  sta movement_counter
+ rts

inc_movement_dir:
  lda movement_dir
  cmp #$03
  beq +
  inc movement_dir
  lda #$00
  sta movement_counter
+ rts

dec_movement_dir:
  lda movement_dir
  cmp #$00
  beq +
  dec movement_dir
  lda #$00
  sta movement_counter
+ rts
  
speed2:
  !byte %00000111,%00000101,%00000111,%00000101,%00000011,%00000110
  !byte %00000110,%00000110,%00000110,%00000111,%00000101,%00000101
  !byte %00000101,%00000100,%00000110,%00000110,%00000101,%00000101
  !byte %00000100,%00000101,%00000010,%00000101,%00000100,%00000101
  
speed3:
  !byte %00001110,%00000111,%00001101,%00001110
  !byte %00001101,%00001010,%00001110,%00001101
  !byte %00001010,%00001010,%00001100,%00001101
  !byte %00001000,%00000110,%00001010,%00001001
  
speed4:
  !byte %00011101,%00011101,%00001110
  !byte %00011010,%00011011,%00010101
  !byte %00010100,%00011010,%00010101
  !byte %00010001,%00001001,%00010001
  
speed6:
  !byte %00111011,%01101110
  !byte %01101010,%01110101
  !byte %01010010,%01100101
  !byte %00100010,%01010001

speed_reset:
  !byte $00
  
;read_speed: test routine for manually changing ball direction and speed
  lda joystick_register
  and #$10 ; fire
  bne +
  lda #$00
  sta speed_reset
+ lda speed_reset
  bne read_speed_return
  lda joystick_register ; reverse logic; 0 = activated
  and #$01 ; up
  bne +
  jsr inc_speed
  inc speed_reset
+ lda joystick_register
  and #$02 ; down
  bne +
  jsr dec_speed
  inc speed_reset
+ lda joystick_register
  and #$04 ; left
  bne +
  jsr dec_movement_dir
  inc speed_reset
+ lda joystick_register
  and #$08 ; right
  bne read_speed_return
  jsr inc_movement_dir
  inc speed_reset
read_speed_return:
  rts

check_bat_ball_collision: ; collision if -5 => delta y => 5; no collision if delta y => 6 and delta y <= 250
  sec
  lda bat_y
  sbc ball_y
  cmp #6
  bcc check_bat_ball_collision_x  ; branch if 0 <= diff <= 5
  cmp #251
  bcs check_bat_ball_collision_x: ; branch if diff >= 251
  rts ; no collision in y axis
check_bat_ball_collision_x: ; collision if -5 >= delta x >= 44; no collision if delta x >= 45 or delta y <= 250
  ; ball_x_position - bat_x_position
  sec
  lda ball_x
  sbc bat_x
  sta movement_temp_x
  lda ball_x_overflow
  sbc bat_x_overflow
  bcc +
  ; diff >= 0
  bne bat_collision_return2 ; diff >= 256
  lda movement_temp_x
  cmp #47
  bcc collision_occurred ; diff < 47
  jmp bat_collision_return
+ ; diff < 0
  lda movement_temp_x
  cmp #251
  bcc bat_collision_return2 ; diff < 251
  ; handle collision 251 <= 0
  lda #$00
  sta pos_x_dir
  lda #$03
  sta movement_dir
  jmp bat_collision
bat_collision_return2:
  rts
collision_occurred:  
  cmp #41 ; 41 >= diff > 47
  bcc +
  lda #$01
  sta pos_x_dir
  lda #$03
  sta movement_dir
  jmp bat_collision
+ cmp #35
  bcc + ; 35 >= diff > 41
  lda #$01
  sta pos_x_dir
  lda #$02
  sta movement_dir
  jmp bat_collision
+ cmp #29
  bcc + ; 29 >= diff > 35
  lda #$01
  sta pos_x_dir
  lda #$01
  sta movement_dir
  jmp bat_collision
+ cmp #21
  bcc + ; 21 >= diff > 29
  lda #$01
  sta pos_x_dir
  lda #$00
  sta movement_dir
  jmp bat_collision
+ cmp #13
  bcc + ; 13 >= diff > 21
  lda #$00
  sta pos_x_dir
  lda #$00
  sta movement_dir
  jmp bat_collision
+ cmp #7
  bcc + ; 7 >= diff > 13
  lda #$00
  sta pos_x_dir
  lda #$01
  sta movement_dir
  jmp bat_collision
+ cmp #1
  bcc + ; 1 >= diff > 7
  lda #$00
  sta pos_x_dir
  lda #$02
  sta movement_dir
  jmp bat_collision
+ bne bat_collision_return ; only possibility diff = 0 (exactly)
  lda #$00
  sta pos_x_dir
  lda #$03
  sta movement_dir
  jmp bat_collision
bat_collision_return:
  rts
  
bat_collision:
  jsr bat_sound
  lda #$00
  sta pos_y_dir ; direction up
  ; update ball_y 
  sec
  lda bat_y 
  sbc #6
  sta ball_y
  rts